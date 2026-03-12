package com.podcast.voice.repository;

import com.podcast.voice.entity.VoiceTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 语音任务数据访问层
 */
@Repository
public interface VoiceTaskRepository extends JpaRepository<VoiceTaskEntity, String> {
    
    /**
     * 根据用户ID和状态查询任务列表
     */
    List<VoiceTaskEntity> findByUserIdAndStatus(String userId, String status);
    
    /**
     * 根据用户ID查询所有任务
     */
    List<VoiceTaskEntity> findByUserId(String userId);
}