import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

const signOutLocally = () => {
  localStorage.clear();
  window.location.href = '/login';
};

/**
 * Get a fresh access token. Refresh tokens work once (the server rotates them, and
 * treats a reused one as stolen), so only one refresh may run at a time: across
 * requests in this tab (the shared promise) and across tabs (a Web Lock). A caller
 * that waited behind another refresh finds the tokens already replaced and uses them.
 */
let refreshInFlight = null;

const LOCK_KEY = 'springpm-refresh-lock';
const LOCK_TTL_MS = 10000; // a tab that dies mid-refresh can't hold the lock for long
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * A cross-tab mutex in localStorage, for when Web Locks aren't available.
 * Claim the lock, wait a moment, and keep it only if no other tab overwrote the claim.
 */
const withStorageLock = async (fn) => {
  const id = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const deadline = Date.now() + LOCK_TTL_MS;
  for (;;) {
    let held = null;
    try { held = JSON.parse(localStorage.getItem(LOCK_KEY) || 'null'); } catch { held = null; }
    if (!held || held.expires < Date.now()) {
      localStorage.setItem(LOCK_KEY, JSON.stringify({ id, expires: Date.now() + LOCK_TTL_MS }));
      await sleep(30);
      try { held = JSON.parse(localStorage.getItem(LOCK_KEY) || 'null'); } catch { held = null; }
      if (held?.id === id) break;
    }
    if (Date.now() > deadline) break; // give up waiting; the server still rejects a reused token
    await sleep(50 + Math.random() * 50);
  }
  try {
    return await fn();
  } finally {
    try {
      if (JSON.parse(localStorage.getItem(LOCK_KEY) || 'null')?.id === id) localStorage.removeItem(LOCK_KEY);
    } catch { /* ignore */ }
  }
};

const refreshAccessToken = (tokenThatFailed) => {
  if (refreshInFlight) return refreshInFlight;

  const doRefresh = async () => {
    const current = localStorage.getItem('accessToken');
    if (current && current !== tokenThatFailed) {
      return current; // another tab or request already refreshed
    }
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) throw new Error('No refresh token');

    const response = await axios.post(
      `${API_BASE_URL}/auth/refresh`,
      { token: refreshToken },
      { headers: { 'Content-Type': 'application/json' } }
    );
    const { accessToken, refreshToken: newRefreshToken } = response.data.data;
    localStorage.setItem('accessToken', accessToken);
    if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken);
    return accessToken;
  };

  // Web Locks only exist on HTTPS and localhost; plain-HTTP dev servers use the storage lock
  const locked = navigator.locks?.request
    ? navigator.locks.request('springpm-token-refresh', doRefresh)
    : withStorageLock(doRefresh);

  refreshInFlight = locked.finally(() => { refreshInFlight = null; });
  return refreshInFlight;
};

// Response interceptor: on 401, refresh once and retry the request
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;
      const tokenThatFailed = (originalRequest.headers?.Authorization || '').replace(/^Bearer /, '');
      try {
        const accessToken = await refreshAccessToken(tokenThatFailed);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        signOutLocally();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

/** Revoke the refresh token on the server. Never throws: signing out always works locally. */
export const revokeSession = async () => {
  const token = localStorage.getItem('refreshToken');
  if (!token) return;
  try {
    await axios.post(`${API_BASE_URL}/auth/logout`, { token }, { headers: { 'Content-Type': 'application/json' } });
  } catch {
    // Offline or already revoked: the local sign-out still happens.
  }
};

export default api;
