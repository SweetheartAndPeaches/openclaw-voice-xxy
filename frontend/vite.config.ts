import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

// https://vitejs.dev/config/
export default define(PWA({
  registerType: 'autoUpdate',
  devOptions: {
    enabled: true
  },
  workbox: {
    globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
    runtimeCaching: [
      {
        urlPattern: /^https:\/\/api\.coze\.com\/voice\/generate/,
        handler: 'NetworkFirst',
        options: {
          cacheName: 'voice-api-cache',
          expiration: {
            maxEntries: 50,
            maxAgeSeconds: 60 * 60 * 24 // 24 hours
          }
        }
      }
    ]
  },
  manifest: {
    name: '口播网站 - AI语音合成平台',
    short_name: '口播网站',
    description: 'AI语音合成平台，支持文本转语音、音色克隆、多语言支持',
    theme_color: '#000000',
    background_color: '#ffffff',
    display: 'standalone',
    scope: '/',
    start_url: '/',
    icons: [
      {
        src: 'logo192.png',
        sizes: '192x192',
        type: 'image/png'
      },
      {
        src: 'logo512.png',
        sizes: '512x512',
        type: 'image/png'
      }
    ]
  }
}));