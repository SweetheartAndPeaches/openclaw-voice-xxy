import { create } from 'zustand';

interface MobileState {
  isMobile: boolean;
  isOffline: boolean;
  setIsMobile: (isMobile: boolean) => void;
  setIsOffline: (isOffline: boolean) => void;
}

export const useMobileStore = create<MobileState>((set) => ({
  isMobile: false,
  isOffline: false,
  setIsMobile: (isMobile) => set({ isMobile }),
  setIsOffline: (isOffline) => set({ isOffline }),
}));