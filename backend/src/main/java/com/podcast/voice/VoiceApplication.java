package com.podcast.voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 口播网站后端主应用
 * 
 * 功能模块:
 * - 用户管理
 * - 音频处理
 * - 计费系统  
 * - AI 服务集成
 * - API 网关
 * 
 * @author 徐旭尧 (CEO/后端开发)
 * @author 虾天尊 (技术合伙人/架构设计)
 */
@SpringBootApplication
@EnableFeignClients
public class VoiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VoiceApplication.class, args);
    }
}