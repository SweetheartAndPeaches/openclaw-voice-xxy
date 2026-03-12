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
        String userId = request.get("userId");
        String text = request.get("text");
        String voiceId = request.get("voiceId");
        
        if (userId == null || text == null || voiceId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        VoiceTaskEntity task = voiceTaskService.createTask(userId, text, voiceId);
        return ResponseEntity.ok(task);
    }
    
    /**
     * 获取任务状态
     * GET /api/voice/tasks/{taskId}
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<VoiceTaskEntity> getTask(@PathVariable String taskId) {
        VoiceTaskEntity task = voiceTaskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }
    
    /**
     * 获取用户任务列表
     * GET /api/voice/tasks?userId={userId}
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<VoiceTaskEntity>> getUserTasks(@RequestParam String userId) {
        List<VoiceTaskEntity> tasks = voiceTaskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }
}