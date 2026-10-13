import axios from 'axios';
import {
  clearAuthTokens,
  getAccessToken,
  setAccessToken,
} from './authSession';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';
const axiosInstance = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshRequest;

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isAuthEndpoint = /\/api\/auth\/(login|register|refresh)$/.test(originalRequest?.url || '');
    if (error.response?.status !== 401 || !originalRequest || originalRequest._retry || isAuthEndpoint) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;
    try {
      refreshRequest ||= axios.post(`${baseURL}/api/auth/refresh`, {}, {
        withCredentials: true,
        headers: { 'Content-Type': 'application/json' },
      }).then((response) => {
        const data = response.data?.data || response.data;
        setAccessToken(data.accessToken);
        window.dispatchEvent(new Event('auth:access-token-updated'));
        return data.accessToken;
      }).finally(() => {
        refreshRequest = null;
      });

      const accessToken = await refreshRequest;
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      clearAuthTokens();
      window.dispatchEvent(new Event('auth:session-expired'));
      return Promise.reject(refreshError);
    }
  },
);

export default axiosInstance;
