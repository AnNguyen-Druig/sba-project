const ACCESS_TOKEN_KEY = 'accessToken';

export const setAccessToken = (token) => {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
};

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);

export const clearLegacyRefreshToken = () => {
  localStorage.removeItem('refreshToken');
};

export const clearAuthTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
};
