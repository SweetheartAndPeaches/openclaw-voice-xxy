package com.podcast.voice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 语音克隆实体类
 * 存储用户上传的音频样本和生成的自定义音色信息
 */
@Entity
@Table(name = "voice_clones")
public class VoiceCloneEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String voiceId;
    
    @Column(nullable = false)
    private String voiceName;
    
    @Column(nullable = false)
    private String audioSampleUrl;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
    
    // Constructors
    public VoiceCloneEntity() {}
    
    public VoiceCloneEntity(String userId, String voiceName, String audioSampleUrl) {
        this.userId = userId;
        this.voiceName = voiceName;
        this.audioSampleUrl = audioSampleUrl;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getVoiceId() {
        return voiceId;
    }
    
    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
    
    public String getVoiceName() {
        return voiceName;
    }
    
    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }
    
    public String getAudioSampleUrl() {
        return audioSampleUrl;
    }
    
    public void setAudioSampleUrl(String audioSampleUrl) {
        this.audioSampleUrl = audioSampleUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}