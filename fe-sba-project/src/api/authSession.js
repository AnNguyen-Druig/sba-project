import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
let accessToken = null;
let refreshPromise = null;

const previousRefreshToken = localStorage.getItem('refreshToken');
if (!localStorage.getItem('token') && previousRefreshToken) {
  localStorage.setItem('token', previousRefreshToken);
}
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');

export const setAccessToken = (token) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

export const clearAuthTokens = () => {
  accessToken = null;
  localStorage.removeItem('token');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
};

export const refreshAccessToken = () => {
  if (refreshPromise) return refreshPromise;

  const refreshToken = localStorage.getItem('token');
  if (!refreshToken) {
    clearAuthTokens();
    return Promise.reject(new Error('Refresh token is not available'));
  }

  refreshPromise = axios.post(`${BASE_URL}/api/auth/refresh-token`, { refreshToken })
    .then((response) => {
      const data = response.data?.data || response.data;
      if (!data.accessToken || !data.refreshToken) {
        throw new Error('Refresh response is missing tokens');
      }

      accessToken = data.accessToken;
      localStorage.setItem('token', data.refreshToken);
      return data;
    })
    .catch((error) => {
      clearAuthTokens();
      localStorage.removeItem('user');
      throw error;
    })
    .finally(() => {
      refreshPromise = null;
    });

  return refreshPromise;
};
