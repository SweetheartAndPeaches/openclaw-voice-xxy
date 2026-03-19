package com.podcast.voice.repository;

import com.podcast.voice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 * 提供用户数据的CRUD操作
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体（如果存在）
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户实体（如果存在）
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * 根据Stripe客户ID查找用户
     * @param customerId Stripe客户ID
     * @return 用户实体（如果存在）
     */
    Optional<UserEntity> findByCustomerId(String customerId);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}