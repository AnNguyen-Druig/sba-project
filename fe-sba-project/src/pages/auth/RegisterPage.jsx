import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from '../../components/common/Toast';
import { authApi } from '../../api/authApi';
import { isValidEmail, isValidPhone } from '../../utils/validators';

const RegisterPage = () => {
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState({ email: '', phone: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!isValidEmail(form.email.trim())) {
      setError('Vui lòng nhập email hợp lệ.');
      return;
    }
    if (form.phone.trim() && !isValidPhone(form.phone.trim())) {
      setError('Số điện thoại không hợp lệ.');
      return;
    }
    if (form.password.length < 8) {
      setError('Mật khẩu phải có ít nhất 8 ký tự.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }

    setLoading(true);
    try {
      await authApi.register({
        email: form.email.trim(),
        phone: form.phone.trim() || null,
        password: form.password,
      });
      toast.success('Đăng ký thành công! Vui lòng đăng nhập.');
      navigate('/login');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: '100%',
    boxSizing: 'border-box',
    marginTop: 6,
    padding: '11px 14px',
    border: '1px solid #ddd',
    borderRadius: 8,
    fontSize: 14,
  };

  return (
    <div style={{
      minHeight: '100vh',
      padding: 20,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #0f3460 0%, #1a1a2e 40%, #00b14f20 100%)',
    }}>
      <div style={{
        width: '100%',
        maxWidth: 420,
        padding: '36px',
        borderRadius: 20,
        background: '#fff',
        boxShadow: '0 20px 60px rgba(0,0,0,0.25)',
      }}>
        <h1 style={{ margin: '0 0 8px', textAlign: 'center', color: '#1a1a2e', fontSize: 24 }}>
          Tạo tài khoản
        </h1>
        <p style={{ margin: '0 0 24px', textAlign: 'center', color: '#888', fontSize: 14 }}>
          Đăng ký tài khoản khách hàng
        </p>

        <form onSubmit={handleSubmit}>
          <label style={{ display: 'block', marginBottom: 16, color: '#444', fontSize: 13, fontWeight: 600 }}>
            Email *
            <input
              type="email"
              required
              autoComplete="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              placeholder="Nhập email"
              style={inputStyle}
            />
          </label>
          <label style={{ display: 'block', marginBottom: 16, color: '#444', fontSize: 13, fontWeight: 600 }}>
            Số điện thoại
            <input
              type="tel"
              autoComplete="tel"
              value={form.phone}
              onChange={(event) => setForm({ ...form, phone: event.target.value })}
              placeholder="Nhập số điện thoại (không bắt buộc)"
              style={inputStyle}
            />
          </label>
          <label style={{ display: 'block', marginBottom: 16, color: '#444', fontSize: 13, fontWeight: 600 }}>
            Mật khẩu *
            <input
              type="password"
              required
              autoComplete="new-password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              placeholder="Ít nhất 8 ký tự"
              style={inputStyle}
            />
          </label>
          <label style={{ display: 'block', marginBottom: 16, color: '#444', fontSize: 13, fontWeight: 600 }}>
            Xác nhận mật khẩu *
            <input
              type="password"
              required
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
              placeholder="Nhập lại mật khẩu"
              style={inputStyle}
            />
          </label>

          {error && (
            <p role="alert" style={{ color: '#e53935', fontSize: 13, margin: '0 0 16px' }}>{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              padding: 13,
              border: 0,
              borderRadius: 10,
              background: loading ? '#66cc99' : '#00b14f',
              color: '#fff',
              fontSize: 15,
              fontWeight: 700,
              cursor: loading ? 'not-allowed' : 'pointer',
            }}
          >
            {loading ? 'Đang đăng ký...' : 'Đăng ký'}
          </button>
        </form>

        <p style={{ margin: '20px 0 0', textAlign: 'center', color: '#666', fontSize: 13 }}>
          Đã có tài khoản?{' '}
          <Link to="/login" style={{ color: '#00b14f', fontWeight: 700, textDecoration: 'none' }}>
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;
