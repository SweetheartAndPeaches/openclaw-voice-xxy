package com.podcast.voice.repository;

import com.podcast.voice.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订阅数据访问层
 * 提供订阅数据的CRUD操作
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    
    /**
     * 根据用户ID查找活跃的订阅
     * @param userId 用户ID
     * @return 活跃的订阅实体（如果存在）
     */
    Optional<SubscriptionEntity> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * 根据用户ID查找所有订阅
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<SubscriptionEntity> findByUserId(Long userId);
    
    /**
     * 根据Stripe订阅ID查找订阅
     * @param stripeSubscriptionId Stripe订阅ID
     * @return 订阅实体（如果存在）
     */
    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    /**
     * 查找用户的当前活跃订阅
     * @param userId 用户ID
     * @return 活跃订阅（如果存在）
     */
    default Optional<SubscriptionEntity> findActiveByUserId(Long userId) {
        return findByUserIdAndStatus(userId, "active");
    }
}