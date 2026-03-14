import { useState, useEffect } from 'react';

/**
 * Custom hook to detect mobile devices and screen sizes
 * Returns true if the device is mobile or tablet
 */
export const useMobile = (): boolean => {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkMobile = () => {
      // Check for mobile browsers
      const isMobileBrowser = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
      
      // Check for small screen sizes (tablet and below)
      const isSmallScreen = window.innerWidth <= 768;
      
      setIsMobile(isMobileBrowser || isSmallScreen);
    };

    // Initial check
    checkMobile();
    
    // Add resize listener
    window.addEventListener('resize', checkMobile);
    
    // Cleanup
    return () => {
      window.removeEventListener('resize', checkMobile);
    };
  }, []);

  return isMobile;
};

/**
 * Custom hook to detect touch support
 */
export const useTouchSupport = (): boolean => {
  const [hasTouch, setHasTouch] = useState(false);

  useEffect(() => {
    const checkTouch = () => {
      setHasTouch('ontouchstart' in window || navigator.maxTouchPoints > 0);
    };

    checkTouch();
  }, []);

  return hasTouch;
};