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
     * 根据模型ID和用户ID查找语音克隆模型
     * @param modelId 模型ID
     * @param userId 用户ID
     * @return 语音克隆模型（如果存在）
     */
    Optional<VoiceCloneEntity> findByModelIdAndUserId(String modelId, String userId);
    
    /**
     * 根据模型ID查找语音克隆模型
     * @param modelId 模型ID
     * @return 语音克隆模型（如果存在）
     */
    Optional<VoiceCloneEntity> findByModelId(String modelId);
    
    /**
     * 检查模型ID是否已存在
     * @param modelId 模型ID
     * @return 是否存在
     */
    boolean existsByModelId(String modelId);
}