package com.podcast.voice.service;

import com.podcast.voice.entity.VoiceTaskEntity;
import com.podcast.voice.repository.VoiceTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    /**
     * 创建新的语音生成任务
     */
    public VoiceTaskEntity createTask(String userId, String text, String voiceId) {
        VoiceTaskEntity task = new VoiceTaskEntity();
        task.setTaskId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        task.setUserId(userId);
        task.setText(text);
        task.setVoiceId(voiceId);
        task.setStatus("pending");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return voiceTaskRepository.save(task);
    }
    
    /**
     * 根据任务ID获取任务
     */
    public VoiceTaskEntity getTaskById(String taskId) {
        return voiceTaskRepository.findById(taskId).orElse(null);
    }
    
    /**
     * 根据用户ID获取任务列表
     */
    public List<VoiceTaskEntity> getTasksByUserId(String userId) {
        return voiceTaskRepository.findByUserId(userId);
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
}