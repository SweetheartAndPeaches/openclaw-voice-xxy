package com.podcast.voice.controller;

import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * 处理用户注册、登录、JWT Token 验证等认证相关操作
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userEntity 用户注册信息
     * @return 注册结果和JWT Token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserEntity userEntity) {
        try {
            UserEntity registeredUser = userService.register(userEntity);
            String token = userService.generateToken(registeredUser.getEmail());
            return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @return 登录结果和JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            if (userService.authenticate(email, password)) {
                String token = userService.generateToken(email);
                UserEntity user = userService.findByEmail(email);
                return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(user);
            } else {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    /**
     * 验证JWT Token
     * @param token JWT Token
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (userService.validateToken(jwtToken)) {
                    String email = userService.getEmailFromToken(jwtToken);
                    return ResponseEntity.ok().body("Valid token for: " + email);
                }
            }
            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * @param token JWT Token
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (userService.validateToken(jwtToken)) {
                    String email = userService.getEmailFromToken(jwtToken);
                    UserEntity user = userService.findByEmail(email);
                    if (user != null) {
                        return ResponseEntity.ok().body(user);
                    }
                }
            }
            return ResponseEntity.status(401).body("Invalid token or user not found");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Failed to get user info: " + e.getMessage());
        }
    }
}