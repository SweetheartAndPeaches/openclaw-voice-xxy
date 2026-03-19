// Advanced Service Worker for PWA support
const CACHE_NAME = 'podcast-website-v2';
const STATIC_CACHE_NAME = 'podcast-static-v2';
const DYNAMIC_CACHE_NAME = 'podcast-dynamic-v2';
const OFFLINE_CACHE_NAME = 'podcast-offline-v2';

// Static assets to cache immediately
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/test-mobile.html',
  '/assets/css/main.css',
  '/assets/js/main.js',
  '/logo.svg'
];

// Offline fallback pages
const OFFLINE_PAGES = [
  '/offline.html'
];

// Install event - cache static assets and offline pages
self.addEventListener('install', (event: any) => {
  console.log('Service Worker installing...');
  
  event.waitUntil(
    Promise.all([
      // Cache static assets
      caches.open(STATIC_CACHE_NAME).then((cache: any) => {
        console.log('Caching static assets');
        return cache.addAll(STATIC_ASSETS);
      }),
      
      // Cache offline pages
      caches.open(OFFLINE_CACHE_NAME).then((cache: any) => {
        console.log('Caching offline pages');
        return cache.addAll(OFFLINE_PAGES);
      })
    ]).then(() => {
      console.log('Service Worker installed successfully');
      // Skip waiting to activate immediately
      return self.skipWaiting();
    })
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', (event: any) => {
  console.log('Service Worker activating...');
  
  const cacheWhitelist = [CACHE_NAME, STATIC_CACHE_NAME, DYNAMIC_CACHE_NAME, OFFLINE_CACHE_NAME];
  
  event.waitUntil(
    caches.keys().then((cacheNames: any) => {
      return Promise.all(
        cacheNames.map((cacheName: any) => {
          if (!cacheWhitelist.includes(cacheName)) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => {
      console.log('Service Worker activated successfully');
      return self.clients.claim();
    })
  );
});

// Fetch event - implement advanced caching strategies
self.addEventListener('fetch', (event: any) => {
  const request = event.request;
  const url = new URL(request.url);
  
  // Skip non-HTTP requests
  if (!(request.url.startsWith('http') || request.url.startsWith('https'))) {
    return;
  }
  
  // Skip API requests that shouldn't be cached (POST, PUT, DELETE)
  if (request.method !== 'GET') {
    event.respondWith(fetch(request));
    return;
  }
  
  // Handle same-origin requests
  if (url.origin === location.origin) {
    // Handle document requests (HTML pages)
    if (request.destination === 'document') {
      event.respondWith(handleDocumentRequest(request));
    }
    // Handle API requests
    else if (request.url.includes('/api/')) {
      event.respondWith(handleApiRequest(request));
    }
    // Handle static assets
    else {
      event.respondWith(handleStaticRequest(request));
    }
  }
  // Handle cross-origin requests (CDN, etc.)
  else {
    event.respondWith(handleCrossOriginRequest(request));
  }
});

// Handle document requests with network-first strategy
async function handleDocumentRequest(request: Request) {
  try {
    // Try network first
    const networkResponse = await fetch(request);
    if (networkResponse.status === 200) {
      // Cache the response
      const cache = await caches.open(DYNAMIC_CACHE_NAME);
      cache.put(request, networkResponse.clone());
    }
    return networkResponse;
  } catch (error) {
    // Fallback to cache
    const cache = await caches.open(DYNAMIC_CACHE_NAME);
    const cachedResponse = await cache.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    // Fallback to offline page
    const offlineCache = await caches.open(OFFLINE_CACHE_NAME);
    const offlineResponse = await offlineCache.match('/offline.html');
    if (offlineResponse) {
      return offlineResponse;
    }
    
    // Return basic offline response
    return new Response('<h1>Offline</h1><p>You are currently offline.</p>', {
      headers: { 'Content-Type': 'text/html' }
    });
  }
}

// Handle API requests with network-first strategy and background sync
async function handleApiRequest(request: Request) {
  try {
    const networkResponse = await fetch(request);
    
    // Only cache successful responses
    if (networkResponse.status === 200) {
      const cache = await caches.open(DYNAMIC_CACHE_NAME);
      cache.put(request, networkResponse.clone());
    }
    
    return networkResponse;
  } catch (error) {
    // Try to return cached response for GET requests
    if (request.method === 'GET') {
      const cache = await caches.open(DYNAMIC_CACHE_NAME);
      const cachedResponse = await cache.match(request);
      if (cachedResponse) {
        return cachedResponse;
      }
    }
    
    // Return error response
    return new Response(JSON.stringify({ error: 'Network error' }), {
      status: 503,
      headers: { 'Content-Type': 'application/json' }
    });
  }
}

// Handle static asset requests with cache-first strategy
async function handleStaticRequest(request: Request) {
  const cache = await caches.open(STATIC_CACHE_NAME);
  
  // Try cache first
  const cachedResponse = await cache.match(request);
  if (cachedResponse) {
    return cachedResponse;
  }
  
  // Fallback to network
  try {
    const networkResponse = await fetch(request);
    if (networkResponse.status === 200) {
      cache.put(request, networkResponse.clone());
    }
    return networkResponse;
  } catch (error) {
    // Return cached response even if stale
    return cachedResponse;
  }
}

// Handle cross-origin requests with network-only strategy
async function handleCrossOriginRequest(request: Request) {
  try {
    return await fetch(request);
  } catch (error) {
    // Return error response
    return new Response(JSON.stringify({ error: 'Cross-origin request failed' }), {
      status: 503,
      headers: { 'Content-Type': 'application/json' }
    });
  }
}

// Background sync for failed requests (if supported)
if ('sync' in self) {
  self.addEventListener('sync', (event: any) => {
    if (event.tag === 'sync-pending-requests') {
      event.waitUntil(syncPendingRequests());
    }
  });
}

async function syncPendingRequests() {
  // This would sync pending requests when back online
  // Implementation depends on how pending requests are stored
  console.log('Syncing pending requests...');
}