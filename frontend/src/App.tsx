import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import VoiceGenerator from './components/VoiceGenerator';
import TaskManager from './components/TaskManager';
import MobileNavigation from './components/MobileNavigation';
import { useMobile } from './hooks/useMobile';
import './App.css';
import './components/Mobile.css';

// Register service worker for PWA
const registerServiceWorker = () => {
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/service-worker.js')
        .then((registration) => {
          console.log('SW registered: ', registration);
        })
        .catch((registrationError) => {
          console.log('SW registration failed: ', registrationError);
        });
    });
  }
};

const ScrollToTop: React.FC = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  return null;
};

const AppContent: React.FC = () => {
  const isMobile = useMobile();
  
  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route path="/" element={<VoiceGenerator />} />
        <Route path="/tasks" element={<TaskManager userId="demo-user" />} />
      </Routes>
      {isMobile && <MobileNavigation />}
    </>
  );
};

const App: React.FC = () => {
  // Register PWA service worker
  useEffect(() => {
    registerServiceWorker();
  }, []);

  return (
    <Router>
      <div className="app">
        <AppContent />
      </div>
    </Router>
  );
};

export default App;