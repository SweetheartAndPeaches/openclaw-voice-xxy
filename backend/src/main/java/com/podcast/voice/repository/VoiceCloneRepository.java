package com.podcast.voice.repository;

import com.podcast.voice.entity.VoiceCloneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 语音克隆数据访问层
 * 提供语音克隆模型的CRUD操作
 */
@Repository
public interface VoiceCloneRepository extends JpaRepository<VoiceCloneEntity, Long> {
    
    /**
     * 根据用户ID查找所有语音克隆模型
     * @param userId 用户ID
     * @return 语音克隆模型列表
     */
    List<VoiceCloneEntity> findByUserId(String userId);
    
    /**
     * 根据音色ID和用户ID查找语音克隆模型
     * @param voiceId 音色ID
     * @param userId 用户ID
     * @return 语音克隆模型（如果存在）
     */
    Optional<VoiceCloneEntity> findByVoiceIdAndUserId(String voiceId, String userId);
    
    /**
     * 根据音色ID查找语音克隆模型
     * @param voiceId 音色ID
     * @return 语音克隆模型（如果存在）
     */
    Optional<VoiceCloneEntity> findByVoiceId(String voiceId);
    
    /**
     * 检查音色ID是否已存在
     * @param voiceId 音色ID
     * @return 是否存在
     */
    boolean existsByVoiceId(String voiceId);
}