import { useState, useCallback, useEffect } from 'react';
import { authApi } from '../api/authApi';
import {
  clearAuthTokens,
  clearLegacyRefreshToken,
  getAccessToken,
  setAccessToken,
} from '../api/authSession';
import { AuthContext } from './auth-context';

const readStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null');
  } catch {
    localStorage.removeItem('user');
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [accessToken, setCurrentAccessToken] = useState(() => getAccessToken());
  const [user, setUser] = useState(() => readStoredUser());

  useEffect(() => {
    clearLegacyRefreshToken();
    const handleExpired = () => {
      clearAuthTokens();
      localStorage.removeItem('user');
      setCurrentAccessToken(null);
      setUser(null);
    };
    const handleAccessTokenUpdate = () => setCurrentAccessToken(getAccessToken());
    window.addEventListener('auth:session-expired', handleExpired);
    window.addEventListener('auth:access-token-updated', handleAccessTokenUpdate);
    return () => {
      window.removeEventListener('auth:session-expired', handleExpired);
      window.removeEventListener('auth:access-token-updated', handleAccessTokenUpdate);
    };
  }, []);

  const saveSession = useCallback((response) => {
    const data = response.data?.data || response.data;
    localStorage.removeItem('refreshToken');
    setAccessToken(data.accessToken);
    setCurrentAccessToken(data.accessToken);
    const userData = {
      id: data.accountId,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
    };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  }, []);

  const login = useCallback(async (credentials) => {
    const response = await authApi.login(credentials);
    return saveSession(response);
  }, [saveSession]);

  const register = useCallback(async (registration) => {
    const response = await authApi.register(registration);
    return saveSession(response);
  }, [saveSession]);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      clearAuthTokens();
      localStorage.removeItem('user');
      setCurrentAccessToken(null);
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        loading: false,
        isAuthenticated: Boolean(accessToken && user),
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
