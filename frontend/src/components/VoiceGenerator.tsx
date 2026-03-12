import React, { useState, useRef, useEffect } from 'react';
import { Howl } from 'howler';
import './VoiceGenerator.css';

interface VoiceGeneratorProps {
  onTaskCreated?: (taskId: string) => void;
}

const VoiceGenerator: React.FC<VoiceGeneratorProps> = ({ onTaskCreated }) => {
  // 文本输入状态
  const [text, setText] = useState<string>('');
  const [wordCount, setWordCount] = useState<number>(0);
  
  // 音色选择状态
  const [voiceId, setVoiceId] = useState<string>('female-01');
  const [language, setLanguage] = useState<string>('zh-CN');
  const [speed, setSpeed] = useState<number>(1.0);
  
  // 任务状态
  const [taskId, setTaskId] = useState<string | null>(null);
  const [taskStatus, setTaskStatus] = useState<'idle' | 'processing' | 'completed' | 'failed'>('idle');
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  
  // 音频播放状态
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [duration, setDuration] = useState<number>(0);
  const [currentTime, setCurrentTime] = useState<number>(0);
  
  // Refs
  const audioRef = useRef<HTMLCanvasElement>(null);
  const howlRef = useRef<Howl | null>(null);
  const animationRef = useRef<number>(0);
  
  // 计算字数
  useEffect(() => {
    const count = text.trim().length;
    setWordCount(count);
  }, [text]);
  
  // 清理音频资源
  useEffect(() => {
    return () => {
      if (howlRef.current) {
        howlRef.current.unload();
      }
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, []);
  
  // 处理生成语音
  const handleGenerateVoice = async () => {
    if (!text.trim()) {
      setErrorMessage('请输入要转换的文本');
      return;
    }
    
    if (text.trim().length > 5000) {
      setErrorMessage('文本长度不能超过5000字符');
      return;
    }
    
    try {
      setTaskStatus('processing');
      setErrorMessage(null);
      
      // 调用后端API创建任务
      const response = await fetch('/api/voice/tasks', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: 'demo-user', // TODO: 从用户会话获取
          text: text.trim(),
          voiceId,
          language,
          speed,
        }),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      setTaskId(data.taskId);
      setTaskStatus('processing');
      
      if (onTaskCreated) {
        onTaskCreated(data.taskId);
      }
      
      // 开始轮询任务状态
      pollTaskStatus(data.taskId);
      
    } catch (error) {
      console.error('Failed to generate voice:', error);
      setErrorMessage('生成语音失败，请稍后重试');
      setTaskStatus('failed');
    }
  };
  
  // 轮询任务状态
  const pollTaskStatus = async (taskId: string) => {
    const maxRetries = 30; // 最多等待30秒
    let retries = 0;
    
    const checkStatus = async () => {
      try {
        const response = await fetch(`/api/voice/tasks/${taskId}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.status === 'completed') {
          setAudioUrl(data.audioUrl);
          setTaskStatus('completed');
          setDuration(data.duration || 0);
        } else if (data.status === 'failed') {
          setErrorMessage('语音生成失败');
          setTaskStatus('failed');
        } else if (retries < maxRetries) {
          retries++;
          setTimeout(checkStatus, 1000); // 每秒检查一次
        } else {
          setErrorMessage('语音生成超时');
          setTaskStatus('failed');
        }
      } catch (error) {
        console.error('Failed to poll task status:', error);
        if (retries < maxRetries) {
          retries++;
          setTimeout(checkStatus, 1000);
        } else {
          setErrorMessage('检查任务状态失败');
          setTaskStatus('failed');
        }
      }
    };
    
    checkStatus();
  };
  
  // 播放音频
  const handlePlayAudio = () => {
    if (!audioUrl) return;
    
    if (howlRef.current) {
      if (isPlaying) {
        howlRef.current.pause();
        setIsPlaying(false);
      } else {
        howlRef.current.play();
        setIsPlaying(true);
      }
    } else {
      // 创建新的 Howl 实例
      const howl = new Howl({
        src: [audioUrl],
        html5: true, // 使用 HTML5 Audio 以支持大文件
        onload: () => {
          setDuration(howl.duration());
          setIsPlaying(true);
          animateWaveform();
        },
        onplay: () => {
          setIsPlaying(true);
          animateWaveform();
        },
        onpause: () => {
          setIsPlaying(false);
          if (animationRef.current) {
            cancelAnimationFrame(animationRef.current);
          }
        },
        onend: () => {
          setIsPlaying(false);
          setCurrentTime(0);
          if (animationRef.current) {
            cancelAnimationFrame(animationRef.current);
          }
        },
        onseek: () => {
          setCurrentTime(howl.seek());
        },
      });
      
      howlRef.current = howl;
      howl.play();
    }
  };
  
  // 绘制波形
  const animateWaveform = () => {
    if (!audioRef.current || !howlRef.current) return;
    
    const canvas = audioRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    const width = canvas.width;
    const height = canvas.height;
    
    // 清空画布
    ctx.clearRect(0, 0, width, height);
    
    // 绘制波形（简化版，实际项目中可以使用 Web Audio API 获取真实波形数据）
    ctx.fillStyle = isPlaying ? '#4f46e5' : '#9ca3af';
    ctx.beginPath();
    
    const currentTimeRatio = howlRef.current.seek() / duration;
    const barCount = 50;
    const barWidth = width / barCount;
    
    for (let i = 0; i < barCount; i++) {
      const barHeight = Math.random() * height * 0.8 + height * 0.1;
      const x = i * barWidth;
      const y = (height - barHeight) / 2;
      
      // 如果是当前播放位置附近，高亮显示
      if (Math.abs(i / barCount - currentTimeRatio) < 0.05) {
        ctx.fillStyle = '#6366f1';
      } else {
        ctx.fillStyle = isPlaying ? '#4f46e5' : '#9ca3af';
      }
      
      ctx.fillRect(x, y, barWidth - 2, barHeight);
    }
    
    // 更新当前时间
    if (isPlaying) {
      setCurrentTime(howlRef.current.seek());
    }
    
    if (isPlaying && howlRef.current.playing()) {
      animationRef.current = requestAnimationFrame(animateWaveform);
    }
  };
  
  // 下载音频
  const handleDownloadAudio = () => {
    if (!audioUrl) return;
    
    const link = document.createElement('a');
    link.href = audioUrl;
    link.download = `voice-${taskId || Date.now()}.mp3`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };
  
  // 重置表单
  const handleReset = () => {
    setText('');
    setTaskId(null);
    setTaskStatus('idle');
    setAudioUrl(null);
    setErrorMessage(null);
    setCurrentTime(0);
    if (howlRef.current) {
      howlRef.current.unload();
      howlRef.current = null;
    }
    setIsPlaying(false);
    if (animationRef.current) {
      cancelAnimationFrame(animationRef.current);
    }
  };
  
  return (
    <div className="voice-generator">
      <h2 className="voice-generator-title">AI 语音生成器</h2>
      
      {/* 文本输入区域 */}
      <div className="voice-generator-section">
        <label htmlFor="text-input" className="voice-generator-label">
          输入文本 ({wordCount} 字符)
        </label>
        <textarea
          id="text-input"
          className="voice-generator-textarea"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="请输入要转换为语音的文本..."
          rows={6}
          disabled={taskStatus === 'processing'}
        />
        <div className="voice-generator-hint">
          支持最多 5000 字符，建议分段处理长文本
        </div>
      </div>
      
      {/* 音色和语言设置 */}
      <div className="voice-generator-section voice-generator-settings">
        <div className="voice-generator-setting-group">
          <label htmlFor="voice-select" className="voice-generator-label">
            音色选择
          </label>
          <select
            id="voice-select"
            className="voice-generator-select"
            value={voiceId}
            onChange={(e) => setVoiceId(e.target.value)}
            disabled={taskStatus === 'processing'}
          >
            <option value="female-01">女声 - 温柔</option>
            <option value="female-02">女声 - 活泼</option>
            <option value="male-01">男声 - 稳重</option>
            <option value="male-02">男声 - 年轻</option>
          </select>
        </div>
        
        <div className="voice-generator-setting-group">
          <label htmlFor="language-select" className="voice-generator-label">
            语言
          </label>
          <select
            id="language-select"
            className="voice-generator-select"
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            disabled={taskStatus === 'processing'}
          >
            <option value="zh-CN">中文 (简体)</option>
            <option value="en-US">English (US)</option>
            <option value="ja-JP">日本語</option>
            <option value="ko-KR">한국어</option>
          </select>
        </div>
        
        <div className="voice-generator-setting-group">
          <label htmlFor="speed-slider" className="voice-generator-label">
            语速: {speed.toFixed(1)}x
          </label>
          <input
            id="speed-slider"
            type="range"
            min="0.5"
            max="2.0"
            step="0.1"
            value={speed}
            onChange={(e) => setSpeed(parseFloat(e.target.value))}
            className="voice-generator-slider"
            disabled={taskStatus === 'processing'}
          />
        </div>
      </div>
      
      {/* 控制按钮 */}
      <div className="voice-generator-controls">
        <button
          className="voice-generator-button voice-generator-button-primary"
          onClick={handleGenerateVoice}
          disabled={taskStatus === 'processing' || !text.trim()}
        >
          {taskStatus === 'processing' ? '生成中...' : '生成语音'}
        </button>
        
        <button
          className="voice-generator-button voice-generator-button-secondary"
          onClick={handleReset}
          disabled={taskStatus === 'idle'}
        >
          重置
        </button>
      </div>
      
      {/* 错误提示 */}
      {errorMessage && (
        <div className="voice-generator-error">
          {errorMessage}
        </div>
      )}
      
      {/* 音频播放器 */}
      {taskStatus === 'completed' && audioUrl && (
        <div className="voice-generator-audio-player">
          <h3 className="voice-generator-subtitle">生成结果</h3>
          
          {/* 波形显示 */}
          <div className="voice-generator-waveform-container">
            <canvas
              ref={audioRef}
              className="voice-generator-waveform"
              width="600"
              height="80"
            />
          </div>
          
          {/* 播放控制 */}
          <div className="voice-generator-playback-controls">
            <button
              className="voice-generator-play-button"
              onClick={handlePlayAudio}
            >
              {isPlaying ? '⏸️ 暂停' : '▶️ 播放'}
            </button>
            
            <div className="voice-generator-time-display">
              {currentTime.toFixed(1)}s / {duration.toFixed(1)}s
            </div>
            
            <button
              className="voice-generator-download-button"
              onClick={handleDownloadAudio}
            >
              📥 下载
            </button>
          </div>
        </div>
      )}
      
      {/* 任务状态 */}
      {taskStatus === 'processing' && (
        <div className="voice-generator-status">
          <div className="voice-generator-status-indicator"></div>
          <span>语音生成中，请稍候...</span>
        </div>
      )}
    </div>
  );
};

export default VoiceGenerator;