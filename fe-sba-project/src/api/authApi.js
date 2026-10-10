import axiosInstance from './axiosInstance';

export const authApi = {
  login: (data) => axiosInstance.post('/api/auth/login', data),
  register: (data) => axiosInstance.post('/api/auth/register', data),
  registerOwner: (data) => axiosInstance.post('/api/auth/register-owner', data),
  logout: (data) => axiosInstance.post('/api/auth/logout', data),
  refreshToken: (data) => axiosInstance.post('/api/auth/refresh-token', data),
};
