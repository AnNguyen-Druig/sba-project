import { useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/authApi';
import { clearAuthTokens, refreshAccessToken, setAccessToken } from '../api/authSession';
import { AuthContext } from './auth-context';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeSession = async () => {
      if (!localStorage.getItem('token')) {
        clearAuthTokens();
        localStorage.removeItem('user');
        setLoading(false);
        return;
      }

      try {
        const data = await refreshAccessToken();
        setUser(data.account);
        localStorage.setItem('user', JSON.stringify(data.account));
      } catch {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initializeSession();
  }, []);

  const login = useCallback(async (credentials) => {
    const res = await authApi.login(credentials);
    const data = res.data?.data || res.data;
    const { accessToken, refreshToken, account: userData } = data;
    clearAuthTokens();
    setAccessToken(accessToken);
    if (refreshToken) localStorage.setItem('token', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  }, []);

  const logout = useCallback(async () => {
    try {
      await refreshAccessToken();
      await authApi.logout({ refreshToken: localStorage.getItem('token') });
    } catch {
      // ignore errors on logout
    } finally {
      clearAuthTokens();
      localStorage.removeItem('user');
      setUser(null);
    }
  }, []);

  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider
      value={{ user, loading, isAuthenticated, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
};
