package com.podcast.voice.controller;

import com.podcast.voice.entity.VoiceTaskEntity;
import com.podcast.voice.service.VoiceTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 语音任务API控制器
 */
@RestController
@RequestMapping("/api/voice")
public class VoiceTaskController {
    
    @Autowired
    private VoiceTaskService voiceTaskService;
    
    /**
     * 创建语音生成任务
     * POST /api/voice/tasks
     */
    @PostMapping("/tasks")
    public ResponseEntity<VoiceTaskEntity> createTask(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String voiceId = request.get("voiceId");
        
        if (text == null || voiceId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            VoiceTaskEntity task = voiceTaskService.createTask(text, voiceId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取任务状态
     * GET /api/voice/tasks/{taskId}
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<VoiceTaskEntity> getTask(@PathVariable String taskId) {
        try {
            VoiceTaskEntity task = voiceTaskService.getTaskById(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
    
    /**
     * 获取当前用户任务列表
     * GET /api/voice/tasks
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<VoiceTaskEntity>> getCurrentUserTasks() {
        try {
            List<VoiceTaskEntity> tasks = voiceTaskService.getCurrentUserTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}