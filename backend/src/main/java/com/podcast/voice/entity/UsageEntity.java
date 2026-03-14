package com.podcast.voice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户用量实体类
 * 用于跟踪用户的API调用、生成时长等用量数据，用于计费
 */
@Entity
@Table(name = "usage_records")
public class UsageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "usage_type", nullable = false)
    private String usageType; // e.g., "voice_generation", "audio_minutes", "api_calls"
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @Column(name = "billing_cycle")
    private String billingCycle; // e.g., "2026-03"
    
    @Column(name = "metadata")
    private String metadata; // JSON string for additional details
    
    // Constructors
    public UsageEntity() {}
    
    public UsageEntity(Long userId, String usageType, Integer quantity) {
        this.userId = userId;
        this.usageType = usageType;
        this.quantity = quantity;
        this.recordedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsageType() {
        return usageType;
    }
    
    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
    
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
    
    public String getBillingCycle() {
        return billingCycle;
    }
    
    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}