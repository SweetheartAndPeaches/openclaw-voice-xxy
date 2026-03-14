package com.podcast.voice.service;

import com.podcast.voice.entity.VoiceCloneEntity;
import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.VoiceCloneRepository;
import com.podcast.voice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 音色克隆服务
 * 处理用户上传的音频文件，创建自定义音色模型
 */
@Service
public class VoiceCloneService {
    
    @Autowired
    private VoiceCloneRepository voiceCloneRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${ai.coze.api-key}")
    private String cozeApiKey;
    
    @Value("${ai.coze.base-url:https://api.coze.com}")
    private String cozeBaseUrl;
    
    @Value("${storage.upload-dir:/tmp/uploads}")
    private String uploadDir;
    
    /**
     * 创建音色克隆任务
     * 
     * @param userId 用户ID
     * @param audioFile 上传的音频文件
     * @param voiceName 自定义音色名称
     * @param description 音色描述
     * @return 创建的音色克隆实体
     */
    public VoiceCloneEntity createVoiceClone(String userId, MultipartFile audioFile, String voiceName, String description) {
        try {
            // 保存上传的音频文件
            String fileName = saveUploadedFile(audioFile);
            
            // 创建音色克隆实体
            VoiceCloneEntity voiceClone = new VoiceCloneEntity();
            voiceClone.setUserId(userId);
            voiceClone.setVoiceName(voiceName);
            voiceClone.setDescription(description);
            voiceClone.setAudioFilePath(fileName);
            voiceClone.setStatus("pending");
            voiceClone.setCreatedAt(new Date());
            voiceClone.setUpdatedAt(new Date());
            
            // 保存到数据库
            VoiceCloneEntity savedVoiceClone = voiceCloneRepository.save(voiceClone);
            
            // 异步处理音色克隆
            processVoiceCloneAsync(savedVoiceClone.getId());
            
            return savedVoiceClone;
            
        } catch (Exception e) {
            throw new RuntimeException("创建音色克隆失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存上传的文件
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
    /**
     * 异步处理音色克隆
     */
    public void processVoiceCloneAsync(Long voiceCloneId) {
        VoiceCloneEntity voiceClone = voiceCloneRepository.findById(voiceCloneId).orElse(null);
        if (voiceClone == null) {
            return;
        }
        
        try {
            // 更新状态为 processing
            voiceClone.setStatus("processing");
            voiceClone.setUpdatedAt(new Date());
            voiceCloneRepository.save(voiceClone);
            
            // 调用 Coze API 进行音色克隆
            String voiceId = cloneVoiceWithCoze(voiceClone.getAudioFilePath(), voiceClone.getVoiceName());
            
            // 更新状态为 completed
            voiceClone.setStatus("completed");
            voiceClone.setVoiceId(voiceId);
            voiceClone.setUpdatedAt(new Date());
            voiceCloneRepository.save(voiceClone);
            
        } catch (Exception e) {
            // 更新状态为 failed
            voiceClone.setStatus("failed");
            voiceClone.setError(e.getMessage());
            voiceClone.setUpdatedAt(new Date());
            voiceCloneRepository.save(voiceClone);
        }
    }
    
    /**
     * 调用 Coze API 进行音色克隆
     */
    private String cloneVoiceWithCoze(String audioFilePath, String voiceName) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(cozeApiKey);
        
        // 读取音频文件
        Path audioPath = Paths.get(uploadDir, audioFilePath);
        byte[] audioBytes;
        try {
            audioBytes = Files.readAllBytes(audioPath);
        } catch (IOException e) {
            throw new RuntimeException("读取音频文件失败: " + e.getMessage(), e);
        }
        
        // 构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return audioFilePath;
            }
        });
        body.add("name", voiceName);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            // 调用 Coze 音色克隆 API
            ResponseEntity<Map> response = restTemplate.exchange(
                cozeBaseUrl + "/voice/clone",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("voice_id")) {
                    return (String) responseBody.get("voice_id");
                } else if (responseBody.containsKey("data")) {
                    Map data = (Map) responseBody.get("data");
                    if (data != null && data.containsKey("voice_id")) {
                        return (String) data.get("voice_id");
                    }
                }
            }
            
            throw new RuntimeException("Coze API 返回无效响应");
            
        } catch (Exception e) {
            throw new RuntimeException("调用 Coze 音色克隆 API 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户的音色克隆列表
     */
    public List<VoiceCloneEntity> getVoiceClonesByUserId(String userId) {
        return voiceCloneRepository.findByUserId(userId);
    }
    
    /**
     * 根据音色ID获取音色克隆信息
     */
    public VoiceCloneEntity getVoiceCloneByVoiceId(String voiceId) {
        return voiceCloneRepository.findByVoiceId(voiceId);
    }
    
    /**
     * 删除音色克隆
     */
    public boolean deleteVoiceClone(Long voiceCloneId, String userId) {
        VoiceCloneEntity voiceClone = voiceCloneRepository.findById(voiceCloneId).orElse(null);
        if (voiceClone == null || !voiceClone.getUserId().equals(userId)) {
            return false;
        }
        
        // 删除音频文件
        try {
            Path audioPath = Paths.get(uploadDir, voiceClone.getAudioFilePath());
            if (Files.exists(audioPath)) {
                Files.delete(audioPath);
            }
        } catch (IOException e) {
            // 记录错误但继续删除数据库记录
            System.err.println("删除音频文件失败: " + e.getMessage());
        }
        
        // 删除数据库记录
        voiceCloneRepository.deleteById(voiceCloneId);
        return true;
    }
    
    // 内部类用于处理文件上传
    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        public ByteArrayResource(byte[] byteArray) {
            super(byteArray);
        }
        
        @Override
        public String getFilename() {
            return super.getFilename();
        }
    }
}