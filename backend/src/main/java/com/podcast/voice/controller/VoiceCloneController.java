package com.podcast.voice.controller;

import com.podcast.voice.entity.VoiceCloneEntity;
import com.podcast.voice.service.VoiceCloneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 音色克隆API控制器
 * 处理用户上传音频、创建自定义音色、管理音色等操作
 */
@RestController
@RequestMapping("/api/voice-clone")
public class VoiceCloneController {
    
    @Autowired
    private VoiceCloneService voiceCloneService;
    
    /**
     * 上传音频文件创建自定义音色
     * POST /api/voice-clone/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<VoiceCloneEntity> uploadVoiceSample(
            Authentication authentication,
            @RequestParam("file") MultipartFile audioFile,
            @RequestParam("name") String voiceName,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            String userId = authentication.getName();
            VoiceCloneEntity voiceClone = voiceCloneService.createVoiceClone(
                userId, audioFile, voiceName, description);
            return ResponseEntity.ok(voiceClone);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取用户的自定义音色列表
     * GET /api/voice-clone
     */
    @GetMapping
    public ResponseEntity<List<VoiceCloneEntity>> getUserVoiceClones(Authentication authentication) {
        String userId = authentication.getName();
        List<VoiceCloneEntity> voiceClones = voiceCloneService.getUserVoiceClones(userId);
        return ResponseEntity.ok(voiceClones);
    }
    
    /**
     * 获取指定音色详情
     * GET /api/voice-clone/{voiceId}
     */
    @GetMapping("/{voiceId}")
    public ResponseEntity<VoiceCloneEntity> getVoiceClone(
            Authentication authentication, 
            @PathVariable String voiceId) {
        
        String userId = authentication.getName();
        VoiceCloneEntity voiceClone = voiceCloneService.getVoiceClone(userId, voiceId);
        if (voiceClone != null) {
            return ResponseEntity.ok(voiceClone);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 删除自定义音色
     * DELETE /api/voice-clone/{voiceId}
     */
    @DeleteMapping("/{voiceId}")
    public ResponseEntity<Void> deleteVoiceClone(
            Authentication authentication, 
            @PathVariable String voiceId) {
        
        String userId = authentication.getName();
        boolean deleted = voiceCloneService.deleteVoiceClone(userId, voiceId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 更新音色信息
     * PUT /api/voice-clone/{voiceId}
     */
    @PutMapping("/{voiceId}")
    public ResponseEntity<VoiceCloneEntity> updateVoiceClone(
            Authentication authentication,
            @PathVariable String voiceId,
            @RequestBody Map<String, String> updates) {
        
        String userId = authentication.getName();
        String name = updates.get("name");
        String description = updates.get("description");
        
        VoiceCloneEntity updated = voiceCloneService.updateVoiceClone(
            userId, voiceId, name, description);
        
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}