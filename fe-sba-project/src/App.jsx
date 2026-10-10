import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './context/auth-context';
import { ToastProvider } from './components/common/Toast';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import { useNavigate } from 'react-router-dom';

import './index.css';

const AppRoutes = () => {
  const { isAuthenticated, loading, user, logout } = useAuth();
  const navigate = useNavigate();

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}>Đang tải...</div>;
  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/" element={
        <main style={{ maxWidth: 640, margin: '80px auto', padding: 32, textAlign: 'center' }}>
          <h1>Đăng nhập thành công</h1>
          <p>{user.email}</p>
          <p>Role: {user.role}</p>
          <button onClick={async () => { await logout(); navigate('/login'); }}>Đăng xuất</button>
        </main>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}

export default App;
