import { useState } from 'react';
import { isValidEmail } from '../../utils/validators';

const InputField = ({ label, type = 'text', value, onChange, error, placeholder, required }) => (
  <div style={{ marginBottom: 16 }}>
    <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: 13, color: '#444' }}>
      {label} {required && <span style={{ color: '#e53935' }}>*</span>}
    </label>
    <input
      type={type}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      style={{
        width: '100%',
        padding: '11px 14px',
        border: `1.5px solid ${error ? '#e53935' : '#ddd'}`,
        borderRadius: 8,
        fontSize: 14,
        color: '#333',
        outline: 'none',
        boxSizing: 'border-box',
        transition: 'border-color 0.2s',
        background: '#fafafa',
      }}
      onFocus={(e) => { if (!error) e.target.style.borderColor = '#00b14f'; }}
      onBlur={(e) => { if (!error) e.target.style.borderColor = '#ddd'; }}
    />
    {error && <p style={{ margin: '4px 0 0', fontSize: 12, color: '#e53935' }}>{error}</p>}
  </div>
);

const LoginForm = ({ onSubmit, loading }) => {
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.email.trim()) e.email = 'Vui lòng nhập email';
    else if (!isValidEmail(form.email)) e.email = 'Email không hợp lệ';
    if (!form.password) e.password = 'Vui lòng nhập mật khẩu';
    return e;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    onSubmit(form);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <InputField
        label="Email"
        type="email"
        value={form.email}
        onChange={(e) => setForm({ ...form, email: e.target.value })}
        error={errors.email}
        placeholder="Nhập email"
        required
      />

      <div style={{ marginBottom: 16 }}>
        <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: 13, color: '#444' }}>
          Mật khẩu <span style={{ color: '#e53935' }}>*</span>
        </label>
        <div style={{ position: 'relative' }}>
          <input
            type={showPassword ? 'text' : 'password'}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder="Nhập mật khẩu"
            style={{
              width: '100%',
              padding: '11px 42px 11px 14px',
              border: `1.5px solid ${errors.password ? '#e53935' : '#ddd'}`,
              borderRadius: 8,
              fontSize: 14,
              color: '#333',
              outline: 'none',
              boxSizing: 'border-box',
              background: '#fafafa',
            }}
          />
          <button
            type="button"
            onClick={() => setShowPassword((p) => !p)}
            style={{
              position: 'absolute',
              right: 12,
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: '#999',
              fontSize: 16,
            }}
          >
            {showPassword ? '🙈' : '👁️'}
          </button>
        </div>
        {errors.password && <p style={{ margin: '4px 0 0', fontSize: 12, color: '#e53935' }}>{errors.password}</p>}
      </div>

      <button
        type="submit"
        disabled={loading}
        style={{
          width: '100%',
          padding: '13px',
          background: loading ? '#66cc99' : '#00b14f',
          color: '#fff',
          border: 'none',
          borderRadius: 10,
          fontSize: 15,
          fontWeight: 700,
          cursor: loading ? 'not-allowed' : 'pointer',
          transition: 'background 0.2s',
          letterSpacing: 0.3,
        }}
      >
        {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
      </button>
    </form>
  );
};

export default LoginForm;
