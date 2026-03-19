package com.podcast.voice.repository;

import com.podcast.voice.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录数据访问层
 */
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    /**
     * 根据用户ID查找支付记录，按创建时间降序排列
     */
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID查找支付记录
     */
    List<PaymentEntity> findByUserId(Long userId);
    
    /**
     * 根据交易ID查找支付记录
     */
    Optional<PaymentEntity> findByTransactionId(String transactionId);
    
    /**
     * 根据支付状态查找支付记录
     */
    List<PaymentEntity> findByStatus(String status);
}