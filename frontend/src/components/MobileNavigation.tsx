import React, { useState } from 'react';
import './Mobile.css';

interface MobileNavigationProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

const MobileNavigation: React.FC<MobileNavigationProps> = ({ activeTab, onTabChange }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const tabs = [
    { id: 'generator', label: '生成器', icon: '🎙️' },
    { id: 'tasks', label: '任务', icon: '📋' },
    { id: 'voices', label: '音色', icon: '🎤' },
    { id: 'account', label: '账户', icon: '👤' }
  ];

  return (
    <div className="mobile-navigation">
      {/* Bottom Navigation Bar */}
      <div className="mobile-nav-bottom">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`mobile-nav-item ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => {
              onTabChange(tab.id);
              setIsMenuOpen(false);
            }}
            aria-label={tab.label}
          >
            <span className="mobile-nav-icon">{tab.icon}</span>
            <span className="mobile-nav-label">{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Hamburger Menu (for additional options) */}
      <button
        className="mobile-hamburger-menu"
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        aria-label="更多选项"
      >
        <span className="hamburger-icon">☰</span>
      </button>

      {/* Expanded Menu */}
      {isMenuOpen && (
        <div className="mobile-expanded-menu">
          <div className="mobile-menu-content">
            <button
              className="mobile-menu-close"
              onClick={() => setIsMenuOpen(false)}
              aria-label="关闭菜单"
            >
              ✕
            </button>
            <div className="mobile-menu-items">
              <button className="mobile-menu-item" onClick={() => onTabChange('settings')}>
                ⚙️ 设置
              </button>
              <button className="mobile-menu-item" onClick={() => onTabChange('help')}>
                ❓ 帮助
              </button>
              <button className="mobile-menu-item" onClick={() => onTabChange('feedback')}>
                💬 反馈
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MobileNavigation;