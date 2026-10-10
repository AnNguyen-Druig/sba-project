import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/auth-context';
import { useToast } from '../../components/common/Toast';
import LoginForm from '../../components/auth/LoginForm';

const AuthCard = ({ children }) => (
  <div
    style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0f3460 0%, #1a1a2e 40%, #00b14f20 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: 20,
    }}
  >
    <div
      style={{
        background: '#fff',
        borderRadius: 20,
        padding: '40px 36px',
        width: '100%',
        maxWidth: 420,
        boxShadow: '0 20px 60px rgba(0,0,0,0.25)',
      }}
    >
      {children}
    </div>
  </div>
);

const LoginPage = () => {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const handleLogin = async (formData) => {
    setLoading(true);
    try {
      await login({
        email: formData.email,
        password: formData.password,
      });
      toast.success('Đăng nhập thành công! Chào mừng bạn trở lại 🎉');
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthCard>
      {/* Logo */}
      <div style={{ textAlign: 'center', marginBottom: 28 }}>
        <div
          style={{
            width: 60,
            height: 60,
            background: 'linear-gradient(135deg, #00b14f, #00803a)',
            borderRadius: 16,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 28,
            margin: '0 auto 12px',
            boxShadow: '0 4px 16px rgba(0,177,79,0.3)',
          }}
        >
          🏘️
        </div>
        <h2 style={{ margin: '0 0 4px', fontSize: 24, fontWeight: 800, color: '#1a1a2e' }}>
          Đăng nhập
        </h2>
        <p style={{ margin: 0, color: '#888', fontSize: 14 }}>
          Quản lý Phòng Trọ — Nền tảng cho thuê phòng
        </p>
      </div>

      <LoginForm onSubmit={handleLogin} loading={loading} />

      <p style={{ textAlign: 'center', marginTop: 20, fontSize: 13, color: '#666' }}>
        Chưa có tài khoản?{' '}
        <Link to="/register" style={{ color: '#00b14f', fontWeight: 700, textDecoration: 'none' }}>
          Đăng ký ngay
        </Link>
      </p>
    </AuthCard>
  );
};

export default LoginPage;
