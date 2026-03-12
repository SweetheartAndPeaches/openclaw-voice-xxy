import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Howl, Howler } from 'howler';
import './AudioPlayer.css';

interface AudioPlayerProps {
  src: string;
  title?: string;
  onPlay?: () => void;
  onPause?: () => void;
  onEnd?: () => void;
  onError?: (error: string) => void;
}

const AudioPlayer: React.FC<AudioPlayerProps> = ({ 
  src, 
  title = '音频播放器', 
  onPlay, 
  onPause, 
  onEnd, 
  onError 
}) => {
  // 播放状态
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [hasError, setHasError] = useState<boolean>(false);
  
  // 音频信息
  const [duration, setDuration] = useState<number>(0);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [volume, setVolume] = useState<number>(1.0);
  const [playbackRate, setPlaybackRate] = useState<number>(1.0);
  const [isLooping, setIsLooping] = useState<boolean>(false);
  
  // 波形数据
  const [waveform, setWaveform] = useState<number[]>([]);
  
  // Refs
  const howlRef = useRef<Howl | null>(null);
  const animationRef = useRef<number>(0);
  const progressRef = useRef<HTMLDivElement>(null);
  const waveformCanvasRef = useRef<HTMLCanvasElement>(null);
  
  // 初始化音频
  useEffect(() => {
    if (!src) {
      setHasError(true);
      onError?.('音频源未提供');
      return;
    }
    
    // 创建新的 Howl 实例
    const howl = new Howl({
      src: [src],
      html5: true,
      preload: true,
      onload: () => {
        setIsLoading(false);
        setDuration(howl.duration());
        generateWaveform(howl.duration());
      },
      onloaderror: (id, error) => {
        console.error('Audio load error:', error);
        setIsLoading(false);
        setHasError(true);
        onError?.('音频加载失败');
      },
      onplay: () => {
        setIsPlaying(true);
        onPlay?.();
        animateWaveform();
      },
      onpause: () => {
        setIsPlaying(false);
        onPause?.();
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
        onEnd?.();
        
        // 如果启用了循环播放，重新开始
        if (isLooping) {
          howl.seek(0);
          howl.play();
        }
      },
      onseek: () => {
        if (howlRef.current) {
          setCurrentTime(howlRef.current.seek());
        }
      }
    });
    
    howlRef.current = howl;
    
    // 清理函数
    return () => {
      if (howlRef.current) {
        howlRef.current.unload();
        howlRef.current = null;
      }
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [src, isLooping, onPlay, onPause, onEnd, onError]);
  
  // 更新音量
  useEffect(() => {
    if (howlRef.current) {
      howlRef.current.volume(volume);
    }
  }, [volume]);
  
  // 更新播放速度
  useEffect(() => {
    if (howlRef.current) {
      howlRef.current.rate(playbackRate);
    }
  }, [playbackRate]);
  
  // 生成波形数据（简化版，实际项目中可以使用 Web Audio API 获取真实波形）
  const generateWaveform = useCallback((duration: number) => {
    const points = 200; // 波形点数
    const waveData = Array.from({ length: points }, () => Math.random() * 0.8 + 0.2);
    setWaveform(waveData);
  }, []);
  
  // 绘制波形
  const drawWaveform = useCallback(() => {
    if (!waveformCanvasRef.current || !waveform.length) return;
    
    const canvas = waveformCanvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    const width = canvas.width;
    const height = canvas.height;
    
    // 清空画布
    ctx.clearRect(0, 0, width, height);
    
    // 绘制波形
    ctx.fillStyle = isPlaying ? '#4f46e5' : '#9ca3af';
    const barWidth = width / waveform.length;
    
    for (let i = 0; i < waveform.length; i++) {
      const barHeight = waveform[i] * height * 0.8;
      const x = i * barWidth;
      const y = (height - barHeight) / 2;
      
      // 如果是当前播放位置附近，高亮显示
      if (duration > 0) {
        const currentTimeRatio = currentTime / duration;
        const barRatio = i / waveform.length;
        if (Math.abs(barRatio - currentTimeRatio) < 0.03) {
          ctx.fillStyle = '#6366f1';
        } else {
          ctx.fillStyle = isPlaying ? '#4f46e5' : '#9ca3af';
        }
      }
      
      ctx.fillRect(x, y, barWidth - 2, barHeight);
    }
  }, [waveform, isPlaying, currentTime, duration]);
  
  // 动画波形
  const animateWaveform = useCallback(() => {
    drawWaveform();
    if (isPlaying && howlRef.current?.playing()) {
      animationRef.current = requestAnimationFrame(animateWaveform);
    }
  }, [drawWaveform, isPlaying]);
  
  // 处理播放/暂停
  const handlePlayPause = () => {
    if (hasError) return;
    
    if (howlRef.current) {
      if (isPlaying) {
        howlRef.current.pause();
      } else {
        howlRef.current.play();
      }
    }
  };
  
  // 处理进度条点击
  const handleProgressClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!progressRef.current || !howlRef.current || hasError) return;
    
    const rect = progressRef.current.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const progress = clickX / rect.width;
    const seekTime = progress * duration;
    
    howlRef.current.seek(seekTime);
    setCurrentTime(seekTime);
  };
  
  // 处理音量变化
  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newVolume = parseFloat(e.target.value);
    setVolume(newVolume);
  };
  
  // 处理播放速度变化
  const handlePlaybackRateChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newRate = parseFloat(e.target.value);
    setPlaybackRate(newRate);
  };
  
  // 切换循环播放
  const toggleLoop = () => {
    setIsLooping(!isLooping);
  };
  
  // 格式化时间显示
  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };
  
  // 键盘快捷键处理
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (hasError) return;
      
      switch (e.code) {
        case 'Space':
          e.preventDefault();
          handlePlayPause();
          break;
        case 'ArrowRight':
          if (howlRef.current && duration > 0) {
            const newTime = Math.min(currentTime + 5, duration);
            howlRef.current.seek(newTime);
            setCurrentTime(newTime);
          }
          break;
        case 'ArrowLeft':
          if (howlRef.current) {
            const newTime = Math.max(currentTime - 5, 0);
            howlRef.current.seek(newTime);
            setCurrentTime(newTime);
          }
          break;
        case 'ArrowUp':
          setVolume(Math.min(volume + 0.1, 1));
          break;
        case 'ArrowDown':
          setVolume(Math.max(volume - 0.1, 0));
          break;
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [hasError, isPlaying, currentTime, duration, volume]);
  
  // 重新加载音频
  const handleReload = () => {
    if (howlRef.current) {
      howlRef.current.unload();
      setHasError(false);
      setIsLoading(true);
      
      const howl = new Howl({
        src: [src],
        html5: true,
        preload: true,
        onload: () => {
          setIsLoading(false);
          setDuration(howl.duration());
          generateWaveform(howl.duration());
        },
        onloaderror: (id, error) => {
          console.error('Audio reload error:', error);
          setIsLoading(false);
          setHasError(true);
          onError?.('音频重新加载失败');
        }
      });
      
      howlRef.current = howl;
    }
  };
  
  return (
    <div className="audio-player">
      <h3 className="audio-player-title">{title}</h3>
      
      {/* 加载状态 */}
      {isLoading && (
        <div className="audio-player-loading">
          <div className="audio-player-loading-spinner"></div>
          <span>加载音频中...</span>
        </div>
      )}
      
      {/* 错误状态 */}
      {hasError && (
        <div className="audio-player-error">
          <div className="audio-player-error-icon">⚠️</div>
          <span>音频加载失败</span>
          <button 
            className="audio-player-retry-button"
            onClick={handleReload}
          >
            重试
          </button>
        </div>
      )}
      
      {/* 主要播放器界面 */}
      {!isLoading && !hasError && (
        <>
          {/* 波形显示 */}
          <div className="audio-player-waveform-container">
            <canvas
              ref={waveformCanvasRef}
              className="audio-player-waveform"
              width="600"
              height="80"
            />
          </div>
          
          {/* 进度条 */}
          <div 
            className="audio-player-progress-container"
            ref={progressRef}
            onClick={handleProgressClick}
          >
            <div 
              className="audio-player-progress-bar"
              style={{ width: `${duration > 0 ? (currentTime / duration) * 100 : 0}%` }}
            ></div>
            <div className="audio-player-progress-handle"></div>
          </div>
          
          {/* 时间显示 */}
          <div className="audio-player-time">
            <span>{formatTime(currentTime)}</span>
            <span>{formatTime(duration)}</span>
          </div>
          
          {/* 控制按钮 */}
          <div className="audio-player-controls">
            <button 
              className="audio-player-control-button"
              onClick={toggleLoop}
              aria-label={isLooping ? "关闭循环播放" : "开启循环播放"}
            >
              {isLooping ? '🔁' : '🔄'}
            </button>
            
            <button 
              className="audio-player-play-button"
              onClick={handlePlayPause}
              disabled={isLoading}
              aria-label={isPlaying ? "暂停" : "播放"}
            >
              {isPlaying ? '⏸️' : '▶️'}
            </button>
            
            <div className="audio-player-volume-control">
              <span>🔊</span>
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={volume}
                onChange={handleVolumeChange}
                className="audio-player-volume-slider"
                aria-label="音量控制"
              />
            </div>
            
            <div className="audio-player-speed-control">
              <label htmlFor="speed-select" className="audio-player-speed-label">
                速度:
              </label>
              <select
                id="speed-select"
                value={playbackRate}
                onChange={handlePlaybackRateChange}
                className="audio-player-speed-select"
                aria-label="播放速度"
              >
                <option value="0.5">0.5x</option>
                <option value="0.75">0.75x</option>
                <option value="1.0">1.0x</option>
                <option value="1.25">1.25x</option>
                <option value="1.5">1.5x</option>
                <option value="2.0">2.0x</option>
              </select>
            </div>
          </div>
          
          {/* 快捷键提示 */}
          <div className="audio-player-shortcuts">
            <span>空格: 播放/暂停</span>
            <span>←→: 快退/快进</span>
            <span>↑↓: 音量调节</span>
          </div>
        </>
      )}
    </div>
  );
};

export default AudioPlayer;