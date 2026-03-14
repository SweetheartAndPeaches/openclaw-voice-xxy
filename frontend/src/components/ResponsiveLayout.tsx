import React from 'react';
import { useMobile } from '../hooks/useMobile';

interface ResponsiveLayoutProps {
  children: React.ReactNode;
}

const ResponsiveLayout: React.FC<ResponsiveLayoutProps> = ({ children }) => {
  const isMobile = useMobile();
  
  return (
    <div className={`responsive-layout ${isMobile ? 'mobile' : 'desktop'}`}>
      {children}
    </div>
  );
};

export default ResponsiveLayout;