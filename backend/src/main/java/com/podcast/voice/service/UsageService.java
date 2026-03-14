package com.podcast.voice.service;

import com.podcast.voice.entity.UsageEntity;
import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.UsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用量服务类 - 跟踪和管理用户资源使用情况
 * 
 * 功能包括：
 * - 记录用户API调用
 * - 查询用户用量统计
 * - 检查用量限制
 * - 生成账单数据
 * 
 * @author OpenClaw-Coder
 */
@Service
public class UsageService {
    
    @Autowired
    private UsageRepository usageRepository;
    
    /**
     * 记录用户用量
     * 
     * @param user 用户实体
     * @param resourceType 资源类型（如 "voice_generation"）
     * @param amount 使用量（如字符数、分钟数等）
     * @param metadata 元数据（JSON格式，可选）
     * @return 用量记录实体
     */
    public UsageEntity recordUsage(UserEntity user, String resourceType, Long amount, String metadata) {
        UsageEntity usage = new UsageEntity();
        usage.setUser(user);
        usage.setResourceType(resourceType);
        usage.setAmount(amount);
        usage.setMetadata(metadata);
        usage.setCreatedAt(LocalDateTime.now());
        
        return usageRepository.save(usage);
    }
    
    /**
     * 记录用户用量（简化版本）
     * 
     * @param user 用户实体
     * @param resourceType 资源类型
     * @param amount 使用量
     * @return 用量记录实体
     */
    public UsageEntity recordUsage(UserEntity user, String resourceType, Long amount) {
        return recordUsage(user, resourceType, amount, null);
    }
    
    /**
     * 获取用户在指定时间段内的总用量
     * 
     * @param userId 用户ID
     * @param resourceType 资源类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总用量
     */
    public Long getTotalUsage(Long userId, String resourceType, LocalDateTime startDate, LocalDateTime endDate) {
        return usageRepository.sumAmountByUserAndResourceTypeAndDateRange(userId, resourceType, startDate, endDate);
    }
    
    /**
     * 获取用户的所有用量记录
     * 
     * @param userId 用户ID
     * @return 用量记录列表
     */
    public List<UsageEntity> getUsageByUser(Long userId) {
        return usageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 获取用户在指定时间段内的用量记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用量记录列表
     */
    public List<UsageEntity> getUsageByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return usageRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
    }
    
    /**
     * 根据ID获取用量记录
     * 
     * @param id 用量记录ID
     * @return 用量记录实体（如果存在）
     */
    public Optional<UsageEntity> findById(Long id) {
        return usageRepository.findById(id);
    }
}