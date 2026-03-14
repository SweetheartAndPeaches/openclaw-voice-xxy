package com.podcast.voice.service;

import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.UserRepository;
import com.podcast.voice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务类 - 处理用户注册、登录、认证等业务逻辑
 * 
 * 功能包括：
 * - 用户注册（密码加密存储）
 * - 用户登录（JWT Token 生成）
 * - 用户信息查询
 * - 密码验证
 * - JWT Token 生成和验证
 * 
 * @author OpenClaw-Coder
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户注册
     * 
     * @param userEntity 用户实体（包含用户名、邮箱、密码）
     * @return 注册成功的用户实体
     * @throws IllegalArgumentException 如果用户名或邮箱已存在
     */
    public UserEntity register(UserEntity userEntity) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userEntity.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + userEntity.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(userEntity.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + userEntity.getEmail());
        }
        
        // 密码加密存储
        userEntity.setPasswordHash(passwordEncoder.encode(userEntity.getPasswordHash()));
        userEntity.setActive(true);
        
        return userRepository.save(userEntity);
    }
    
    /**
     * 用户登录验证
     * 
     * @param email 邮箱
     * @param password 密码（明文）
     * @return 验证是否成功
     */
    public boolean authenticate(String email, String password) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // 验证密码
            return passwordEncoder.matches(password, user.getPasswordHash());
        }
        
        return false;
    }
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户实体，如果不存在返回 null
     */
    public UserEntity findByEmail(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null);
    }
    
    /**
     * 生成 JWT Token
     * 
     * @param email 用户邮箱
     * @return JWT Token
     */
    public String generateToken(String email) {
        return jwtUtil.generateToken(email);
    }
    
    /**
     * 验证 JWT Token
     * 
     * @param token JWT Token
     * @return 验证是否成功
     */
    public boolean validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, email);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从 JWT Token 中提取邮箱
     * 
     * @param token JWT Token
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
}