package com.podcast.voice.service;

import com.podcast.voice.entity.VoiceTaskEntity;
import com.podcast.voice.repository.VoiceTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 语音任务业务逻辑层
 */
@Service
public class VoiceTaskService {
    
    @Autowired
    private VoiceTaskRepository voiceTaskRepository;
    
    @Autowired
    private AiVoiceService aiVoiceService;
    
    @Autowired
    private UsageService usageService;
    
    /**
     * 创建新的语音生成任务
     */
    public VoiceTaskEntity createTask(String text, String voiceId) {
        // 获取当前认证用户
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未认证");
        }
        
        // 检查用户是否还有可用额度
        if (!usageService.hasAvailableQuota(currentUserId)) {
            throw new RuntimeException("配额不足，请升级订阅计划");
        }
        
        VoiceTaskEntity task = new VoiceTaskEntity();
        task.setTaskId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        task.setUserId(currentUserId);
        task.setText(text);
        task.setVoiceId(voiceId);
        task.setStatus("pending");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // 保存任务
        VoiceTaskEntity savedTask = voiceTaskRepository.save(task);
        
        // 异步处理语音生成（在实际应用中，这应该通过消息队列或异步任务处理）
        // 这里简化为直接调用
        try {
            VoiceTaskEntity processedTask = aiVoiceService.processVoiceTask(savedTask);
            // 记录用量
            usageService.recordUsage(currentUserId, processedTask.getDuration());
            return processedTask;
        } catch (Exception e) {
            // 处理失败
            savedTask.setStatus("failed");
            savedTask.setUpdatedAt(LocalDateTime.now());
            return voiceTaskRepository.save(savedTask);
        }
    }
    
    /**
     * 根据任务ID获取任务
     */
    public VoiceTaskEntity getTaskById(String taskId) {
        VoiceTaskEntity task = voiceTaskRepository.findById(taskId).orElse(null);
        if (task != null) {
            // 验证用户是否有权限访问此任务
            String currentUserId = getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(task.getUserId())) {
                throw new RuntimeException("无权访问此任务");
            }
        }
        return task;
    }
    
    /**
     * 根据用户ID获取任务列表
     */
    public List<VoiceTaskEntity> getTasksByUserId(String userId) {
        // 如果传入的userId与当前用户不匹配，需要管理员权限
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || (!"admin".equals(getCurrentUserRole()) && !currentUserId.equals(userId))) {
            throw new RuntimeException("无权访问此用户任务");
        }
        return voiceTaskRepository.findByUserId(userId);
    }
    
    /**
     * 获取当前用户的任务列表
     */
    public List<VoiceTaskEntity> getCurrentUserTasks() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未认证");
        }
        return voiceTaskRepository.findByUserId(currentUserId);
    }
    
    /**
     * 更新任务状态
     */
    public VoiceTaskEntity updateTaskStatus(String taskId, String status, String audioUrl, Long duration) {
        VoiceTaskEntity task = voiceTaskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(status);
            task.setAudioUrl(audioUrl);
            task.setDuration(duration);
            task.setUpdatedAt(LocalDateTime.now());
            return voiceTaskRepository.save(task);
        }
        return null;
    }
    
    /**
     * 获取当前认证用户ID
     */
    private String getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        } catch (Exception e) {
            // 认证上下文不可用
        }
        return null;
    }
    
    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole() {
        // 在实际应用中，这应该从用户详情中获取
        // 这里简化为检查用户名是否为admin
        String currentUserId = getCurrentUserId();
        if ("admin".equals(currentUserId)) {
            return "admin";
        }
        return "user";
    }
}