import React, { useState, useEffect } from 'react';
import './PWAInstallPrompt.css';

interface PWAInstallPromptProps {
  onInstall?: () => void;
  onCancel?: () => void;
}

const PWAInstallPrompt: React.FC<PWAInstallPromptProps> = ({ onInstall, onCancel }) => {
  const [showPrompt, setShowPrompt] = useState(false);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);

  useEffect(() => {
    // Listen for the beforeinstallprompt event
    const handleBeforeInstallPrompt = (e: any) => {
      // Prevent the mini-infobar from appearing on mobile
      e.preventDefault();
      // Stash the event so it can be triggered later
      setDeferredPrompt(e);
      // Show the install prompt
      setShowPrompt(true);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  const handleInstallClick = async () => {
    if (deferredPrompt) {
      // Show the install prompt
      deferredPrompt.prompt();
      // Wait for the user to respond to the prompt
      const { outcome } = await deferredPrompt.userChoice;
      
      if (onInstall) {
        onInstall();
      }
      
      // Hide the prompt
      setShowPrompt(false);
      setDeferredPrompt(null);
    }
  };

  const handleCancelClick = () => {
    setShowPrompt(false);
    setDeferredPrompt(null);
    
    if (onCancel) {
      onCancel();
    }
  };

  if (!showPrompt) {
    return null;
  }

  return (
    <div className="pwa-install-prompt">
      <div className="pwa-install-prompt-content">
        <h3 className="pwa-install-title">安装应用</h3>
        <p className="pwa-install-description">
          将 Podcast AI 添加到主屏幕，获得类似原生应用的体验！
        </p>
        <div className="pwa-install-buttons">
          <button 
            className="pwa-install-button pwa-install-button-primary"
            onClick={handleInstallClick}
          >
            安装
          </button>
          <button 
            className="pwa-install-button pwa-install-button-secondary"
            onClick={handleCancelClick}
          >
            取消
          </button>
        </div>
      </div>
    </div>
  );
};

export default PWAInstallPrompt;