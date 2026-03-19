import React, { useState, useRef, useEffect } from 'react';
import { useMobile } from '../hooks/useMobile';
import './VoiceCloner.css';

interface VoiceClonerProps {
  onVoiceCreated?: (voiceId: string) => void;
}

const VoiceCloner: React.FC<VoiceClonerProps> = ({ onVoiceCreated }) => {
  const isMobile = useMobile();
  
  // Upload state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileName, setFileName] = useState<string>('');
  const [fileSize, setFileSize] = useState<number>(0);
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  
  // Voice info state
  const [voiceName, setVoiceName] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  
  // Cloning state
  const [isCloning, setIsCloning] = useState<boolean>(false);
  const [cloningStatus, setCloningStatus] = useState<'idle' | 'uploading' | 'processing' | 'completed' | 'failed'>('idle');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  // User's custom voices
  const [customVoices, setCustomVoices] = useState<any[]>([]);
  const [loadingVoices, setLoadingVoices] = useState<boolean>(true);
  
  // Refs
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // Load user's custom voices on mount
  useEffect(() => {
    loadCustomVoices();
  }, []);
  
  const loadCustomVoices = async () => {
    try {
      setLoadingVoices(true);
      const response = await fetch('/api/voice-clone');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const voices = await response.json();
      setCustomVoices(voices);
    } catch (error) {
      console.error('Failed to load custom voices:', error);
    } finally {
      setLoadingVoices(false);
    }
  };
  
  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // Validate file type and size
      if (!file.type.startsWith('audio/')) {
        setErrorMessage('请上传音频文件（MP3, WAV, etc.）');
        return;
      }
      
      if (file.size > 10 * 1024 * 1024) { // 10MB limit
        setErrorMessage('文件大小不能超过10MB');
        return;
      }
      
      setSelectedFile(file);
      setFileName(file.name);
      setFileSize(file.size);
      setErrorMessage(null);
    }
  };
  
  const handleFileClick = () => {
    fileInputRef.current?.click();
  };
  
  const handleCreateVoiceClone = async () => {
    if (!selectedFile) {
      setErrorMessage('请选择要上传的音频文件');
      return;
    }
    
    if (!voiceName.trim()) {
      setErrorMessage('请输入自定义音色名称');
      return;
    }
    
    try {
      setIsCloning(true);
      setCloningStatus('uploading');
      setErrorMessage(null);
      setSuccessMessage(null);
      
      // Create form data for file upload
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('name', voiceName.trim());
      if (description.trim()) {
        formData.append('description', description.trim());
      }
      
      // Upload file and create voice clone
      const response = await fetch('/api/voice-clone/upload', {
        method: 'POST',
        body: formData,
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }
      
      const voiceClone = await response.json();
      
      // Start polling for processing status
      setCloningStatus('processing');
      pollVoiceCloneStatus(voiceClone.id);
      
    } catch (error: any) {
      console.error('Failed to create voice clone:', error);
      setErrorMessage(error.message || '创建自定义音色失败，请稍后重试');
      setCloningStatus('failed');
      setIsCloning(false);
    }
  };
  
  const pollVoiceCloneStatus = async (voiceCloneId: number) => {
    const maxRetries = 60; // Wait up to 60 seconds
    let retries = 0;
    
    const checkStatus = async () => {
      try {
        const response = await fetch(`/api/voice-clone/${voiceCloneId}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const voiceClone = await response.json();
        
        if (voiceClone.status === 'completed') {
          setCloningStatus('completed');
          setSuccessMessage('自定义音色创建成功！');
          setIsCloning(false);
          
          // Refresh custom voices list
          loadCustomVoices();
          
          if (onVoiceCreated && voiceClone.voiceId) {
            onVoiceCreated(voiceClone.voiceId);
          }
        } else if (voiceClone.status === 'failed') {
          setErrorMessage(voiceClone.error || '音色克隆处理失败');
          setCloningStatus('failed');
          setIsCloning(false);
        } else if (retries < maxRetries) {
          retries++;
          setTimeout(checkStatus, 1000); // Check every second
        } else {
          setErrorMessage('音色克隆处理超时');
          setCloningStatus('failed');
          setIsCloning(false);
        }
      } catch (error) {
        console.error('Failed to poll voice clone status:', error);
        if (retries < maxRetries) {
          retries++;
          setTimeout(checkStatus, 1000);
        } else {
          setErrorMessage('检查音色状态失败');
          setCloningStatus('failed');
          setIsCloning(false);
        }
      }
    };
    
    checkStatus();
  };
  
  const handleDeleteVoice = async (voiceId: string) => {
    if (!confirm('确定要删除这个自定义音色吗？此操作无法撤销。')) {
      return;
    }
    
    try {
      const response = await fetch(`/api/voice-clone/${voiceId}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      // Refresh custom voices list
      loadCustomVoices();
    } catch (error) {
      console.error('Failed to delete voice clone:', error);
      alert('删除自定义音色失败，请稍后重试');
    }
  };
  
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };
  
  const resetForm = () => {
    setSelectedFile(null);
    setFileName('');
    setFileSize(0);
    setVoiceName('');
    setDescription('');
    setErrorMessage(null);
    setSuccessMessage(null);
    setCloningStatus('idle');
    setIsCloning(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };
  
  return (
    <div className="voice-cloner">
      <h2 className="voice-cloner-title">自定义音色克隆</h2>
      
      {/* Upload Section */}
      <div className="voice-cloner-section">
        <h3 className="voice-cloner-subtitle">上传音频样本</h3>
        
        <div 
          className={`voice-cloner-upload-area ${selectedFile ? 'selected' : ''}`}
          onClick={handleFileClick}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept="audio/*"
            onChange={handleFileSelect}
            style={{ display: 'none' }}
          />
          
          {selectedFile ? (
            <div className="voice-cloner-file-info">
              <div className="voice-cloner-file-name">{fileName}</div>
              <div className="voice-cloner-file-size">{formatFileSize(fileSize)}</div>
              {uploadProgress > 0 && uploadProgress < 100 && (
                <div className="voice-cloner-upload-progress">
                  <div 
                    className="voice-cloner-progress-bar"
                    style={{ width: `${uploadProgress}%` }}
                  ></div>
                </div>
              )}
            </div>
          ) : (
            <div className="voice-cloner-upload-placeholder">
              <div className="voice-cloner-upload-icon">🎤</div>
              <p>点击选择音频文件或拖拽文件到此处</p>
              <p className="voice-cloner-upload-hint">支持 MP3, WAV, OGG 等格式，最大 10MB</p>
            </div>
          )}
        </div>
      </div>
      
      {/* Voice Info Section */}
      <div className="voice-cloner-section">
        <h3 className="voice-cloner-subtitle">音色信息</h3>
        
        <div className="voice-cloner-form-group">
          <label htmlFor="voice-name" className="voice-cloner-label">
            音色名称 *
          </label>
          <input
            id="voice-name"
            type="text"
            className="voice-cloner-input"
            value={voiceName}
            onChange={(e) => setVoiceName(e.target.value)}
            placeholder="例如：我的声音、客服小张等"
            disabled={isCloning}
          />
        </div>
        
        <div className="voice-cloner-form-group">
          <label htmlFor="voice-description" className="voice-cloner-label">
            音色描述
          </label>
          <textarea
            id="voice-description"
            className="voice-cloner-textarea"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="可选：描述音色特点，如语调、情感等"
            rows={isMobile ? 2 : 3}
            disabled={isCloning}
          />
        </div>
      </div>
      
      {/* Action Buttons */}
      <div className="voice-cloner-actions">
        <button
          className="voice-cloner-button voice-cloner-button-primary"
          onClick={handleCreateVoiceClone}
          disabled={isCloning || !selectedFile || !voiceName.trim()}
        >
          {isCloning ? '处理中...' : '创建自定义音色'}
        </button>
        
        <button
          className="voice-cloner-button voice-cloner-button-secondary"
          onClick={resetForm}
          disabled={cloningStatus === 'idle' && !selectedFile && !voiceName}
        >
          重置
        </button>
      </div>
      
      {/* Status Messages */}
      {errorMessage && (
        <div className="voice-cloner-error">
          {errorMessage}
        </div>
      )}
      
      {successMessage && (
        <div className="voice-cloner-success">
          {successMessage}
        </div>
      )}
      
      {/* Status Indicator */}
      {cloningStatus === 'processing' && (
        <div className="voice-cloner-status">
          <div className="voice-cloner-status-indicator"></div>
          <span>正在处理音频并生成自定义音色，请稍候...</span>
        </div>
      )}
      
      {/* Custom Voices List */}
      <div className="voice-cloner-section">
        <h3 className="voice-cloner-subtitle">
          我的自定义音色 {loadingVoices && '(加载中...)'}
        </h3>
        
        {loadingVoices ? (
          <div className="voice-cloner-loading">加载中...</div>
        ) : customVoices.length === 0 ? (
          <div className="voice-cloner-empty">
            还没有创建自定义音色。上传音频样本来创建你的专属音色！
          </div>
        ) : (
          <div className="voice-cloner-voices-grid">
            {customVoices.map((voice) => (
              <div key={voice.id} className="voice-cloner-voice-card">
                <div className="voice-cloner-voice-header">
                  <h4 className="voice-cloner-voice-name">{voice.voiceName}</h4>
                  <span className={`voice-cloner-voice-status ${voice.status}`}>
                    {voice.status === 'completed' ? '✓ 就绪' : 
                     voice.status === 'processing' ? '🔄 处理中' : 
                     voice.status === 'failed' ? '✗ 失败' : '⏳ 待处理'}
                  </span>
                </div>
                
                {voice.description && (
                  <p className="voice-cloner-voice-description">{voice.description}</p>
                )}
                
                <div className="voice-cloner-voice-footer">
                  <small className="voice-cloner-voice-date">
                    创建于: {new Date(voice.createdAt).toLocaleString()}
                  </small>
                  
                  {voice.status === 'completed' && (
                    <button
                      className="voice-cloner-voice-use-button"
                      onClick={() => onVoiceCreated && onVoiceCreated(voice.voiceId)}
                    >
                      使用
                    </button>
                  )}
                  
                  <button
                    className="voice-cloner-voice-delete-button"
                    onClick={() => handleDeleteVoice(voice.voiceId)}
                    disabled={voice.status === 'processing'}
                  >
                    删除
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default VoiceCloner;