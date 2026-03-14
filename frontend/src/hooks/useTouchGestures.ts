import { useState, useRef, useEffect } from 'react';

interface TouchGestureState {
  isSwiping: boolean;
  swipeDirection: 'left' | 'right' | 'up' | 'down' | null;
  swipeDistance: number;
  touchStart: { x: number; y: number } | null;
  touchEnd: { x: number; y: number } | null;
}

interface UseTouchGesturesOptions {
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
  onSwipeUp?: () => void;
  onSwipeDown?: () => void;
  threshold?: number;
}

export const useTouchGestures = (options: UseTouchGesturesOptions = {}) => {
  const {
    onSwipeLeft,
    onSwipeRight,
    onSwipeUp,
    onSwipeDown,
    threshold = 50
  } = options;

  const [gestureState, setGestureState] = useState<TouchGestureState>({
    isSwiping: false,
    swipeDirection: null,
    swipeDistance: 0,
    touchStart: null,
    touchEnd: null
  });

  const touchRef = useRef<HTMLDivElement>(null);

  const handleTouchStart = (e: TouchEvent) => {
    const touch = e.touches[0];
    setGestureState(prev => ({
      ...prev,
      touchStart: { x: touch.clientX, y: touch.clientY },
      touchEnd: null,
      isSwiping: false,
      swipeDirection: null,
      swipeDistance: 0
    }));
  };

  const handleTouchMove = (e: TouchEvent) => {
    if (!gestureState.touchStart) return;

    const touch = e.touches[0];
    const touchEnd = { x: touch.clientX, y: touch.clientY };
    
    const deltaX = touchEnd.x - gestureState.touchStart.x;
    const deltaY = touchEnd.y - gestureState.touchStart.y;
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    let direction: 'left' | 'right' | 'up' | 'down' | null = null;
    
    if (Math.abs(deltaX) > Math.abs(deltaY)) {
      // Horizontal swipe
      direction = deltaX > 0 ? 'right' : 'left';
    } else {
      // Vertical swipe
      direction = deltaY > 0 ? 'down' : 'up';
    }

    setGestureState(prev => ({
      ...prev,
      touchEnd,
      isSwiping: distance > 10,
      swipeDirection: direction,
      swipeDistance: distance
    }));
  };

  const handleTouchEnd = () => {
    if (!gestureState.touchStart || !gestureState.touchEnd) return;

    const deltaX = gestureState.touchEnd.x - gestureState.touchStart.x;
    const deltaY = gestureState.touchEnd.y - gestureState.touchStart.y;
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    if (distance > threshold) {
      switch (gestureState.swipeDirection) {
        case 'left':
          onSwipeLeft?.();
          break;
        case 'right':
          onSwipeRight?.();
          break;
        case 'up':
          onSwipeUp?.();
          break;
        case 'down':
          onSwipeDown?.();
          break;
      }
    }

    setGestureState(prev => ({
      ...prev,
      isSwiping: false,
      swipeDirection: null,
      swipeDistance: 0,
      touchStart: null,
      touchEnd: null
    }));
  };

  useEffect(() => {
    const currentRef = touchRef.current;
    if (!currentRef) return;

    currentRef.addEventListener('touchstart', handleTouchStart);
    currentRef.addEventListener('touchmove', handleTouchMove);
    currentRef.addEventListener('touchend', handleTouchEnd);

    return () => {
      if (currentRef) {
        currentRef.removeEventListener('touchstart', handleTouchStart);
        currentRef.removeEventListener('touchmove', handleTouchMove);
        currentRef.removeEventListener('touchend', handleTouchEnd);
      }
    };
  }, [gestureState.touchStart, gestureState.touchEnd, threshold]);

  return {
    ...gestureState,
    touchRef
  };
};