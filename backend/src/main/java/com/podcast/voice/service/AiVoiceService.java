package com.podcast.voice.service;

import com.podcast.voice.entity.UsageEntity;
import com.podcast.voice.entity.VoiceCloneEntity;
import com.podcast.voice.entity.VoiceTaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI 语音生成服务
 * 集成 Coze Voice Gen API 实现真实的语音生成功能
 * 支持标准音色和自定义克隆音色
 */
@Service
public class AiVoiceService {
    
    @Value("${coze.api-key}")
    private String cozeApiKey;
    
    @Value("${coze.base-url:https://api.coze.com}")
    private String cozeBaseUrl;
    
    @Value("${ai.voice-gen.endpoint:https://api.coze.com/voice/generate}")
    private String voiceGenEndpoint;
    
    private final RestTemplate restTemplate;
    
    @Autowired
    private UsageService usageService;
    
    @Autowired
    private VoiceCloneService voiceCloneService;
    
    public AiVoiceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 调用 Coze Voice Gen API 生成语音
     * 
     * @param text 要转换的文本
     * @param voiceId 音色ID
     * @param language 语言
     * @param speed 语速 (0.5-2.0)
     * @param userId 用户ID（用于用量统计）
     * @return 生成的音频文件URL
     */
    public String generateVoice(String text, String voiceId, String language, Double speed, String userId) {
        // 检查是否是自定义克隆音色
        VoiceCloneEntity clone = voiceCloneService.findByCloneId(voiceId);
        if (clone != null) {
            // 验证用户是否有权限使用此克隆音色
            if (!clone.getUserId().equals(userId)) {
                throw new RuntimeException("无权使用此克隆音色");
            }
            // 使用克隆音色的原始音色ID
            voiceId = clone.getOriginalVoiceId();
        }
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cozeApiKey);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("voice_id", voiceId);
        requestBody.put("language", language);
        requestBody.put("speed", speed != null ? speed : 1.0);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            // 调用 Coze API
            ResponseEntity<Map> response = restTemplate.exchange(
                voiceGenEndpoint,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 从响应中提取音频URL
                Map<String, Object> responseBody = response.getBody();
                String audioUrl = null;
                
                if (responseBody.containsKey("audio_url")) {
                    audioUrl = (String) responseBody.get("audio_url");
                } else if (responseBody.containsKey("data")) {
                    // 处理嵌套的data结构
                    Map data = (Map) responseBody.get("data");
                    if (data != null && data.containsKey("audio_url")) {
                        audioUrl = (String) data.get("audio_url");
                    }
                }
                
                if (audioUrl != null) {
                    // 记录用量
                    Long duration = calculateDuration(text);
                    usageService.recordUsage(userId, "voice_generation", duration.intValue());
                    return audioUrl;
                }
            }
            
            throw new RuntimeException("Coze API 返回无效响应");
            
        } catch (Exception e) {
            throw new RuntimeException("调用 Coze Voice Gen API 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 异步处理语音生成任务
     * 
     * @param task 语音任务实体
     * @return 处理后的任务实体
     */
    public VoiceTaskEntity processVoiceTask(VoiceTaskEntity task) {
        try {
            // 更新任务状态为 processing
            task.setStatus("processing");
            
            // 调用 AI 语音生成
            String audioUrl = generateVoice(
                task.getText(),
                task.getVoiceId(),
                "zh-CN", // 默认中文，可根据需求扩展
                1.0,     // 默认语速，可根据需求扩展
                task.getUserId()
            );
            
            // 更新任务状态为 completed
            task.setStatus("completed");
            task.setAudioUrl(audioUrl);
            task.setDuration(calculateDuration(task.getText()));
            
        } catch (Exception e) {
            // 更新任务状态为 failed
            task.setStatus("failed");
            task.setAudioUrl(null);
            task.setDuration(0L);
        }
        
        return task;
    }
    
    /**
     * 根据文本估算音频时长（粗略估算）
     * 
     * @param text 文本内容
     * @return 估算的时长（秒）
     */
    private Long calculateDuration(String text) {
        if (text == null || text.isEmpty()) {
            return 0L;
        }
        
        // 中文字符按每分钟200字估算，英文按每分钟150词估算
        int chineseChars = 0;
        int englishWords = 0;
        
        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chineseChars++;
            }
        }
        
        String[] words = text.replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                englishWords++;
            }
        }
        
        double duration = (chineseChars / 200.0) * 60 + (englishWords / 150.0) * 60;
        return Math.max(1L, Math.round(duration));
    }
}