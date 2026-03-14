package com.podcast.voice.repository;

import com.podcast.voice.entity.UsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用量数据访问层
 * 提供用量数据的CRUD操作和统计查询
 */
@Repository
public interface UsageRepository extends JpaRepository<UsageEntity, Long> {
    
    /**
     * 根据用户ID查找用量记录
     * @param userId 用户ID
     * @return 用量记录列表
     */
    List<UsageEntity> findByUserId(Long userId);
    
    /**
     * 根据用户ID和时间范围查找用量记录
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 用量记录列表
     */
    List<UsageEntity> findByUserIdAndCreatedAtBetween(
        Long userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    /**
     * 获取用户在指定时间段内的总用量（字符数）
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 总字符数
     */
    @Query("SELECT SUM(u.characterCount) FROM UsageEntity u " +
           "WHERE u.userId = :userId " +
           "AND u.createdAt BETWEEN :startDate AND :endDate")
    Long getTotalCharacterCountByUserAndPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 检查用户是否超出免费额度
     * @param userId 用户ID
     * @param freeTierLimit 免费额度限制
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 是否超出免费额度
     */
    @Query("SELECT SUM(u.characterCount) > :freeTierLimit FROM UsageEntity u " +
           "WHERE u.userId = :userId " +
           "AND u.createdAt BETWEEN :startDate AND :endDate")
    Boolean isUserExceedingFreeTier(
        @Param("userId") Long userId,
        @Param("freeTierLimit") Long freeTierLimit,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}