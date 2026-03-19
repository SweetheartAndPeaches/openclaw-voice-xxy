package com.podcast.voice.service;

import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Spring Security UserDetailsService 实现
 * 用于加载用户详细信息以进行认证
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 首先尝试按用户名查找
        UserEntity user = userRepository.findByUsername(username);
        
        // 如果按用户名没找到，尝试按邮箱查找（支持邮箱登录）
        if (user == null) {
            user = userRepository.findByEmail(username);
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        // 返回 Spring Security 的 UserDetails 对象
        return new User(user.getEmail(), user.getPasswordHash(), new ArrayList<>());
    }
}