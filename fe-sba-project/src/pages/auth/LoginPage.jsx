import { useState } from 'react';
import { ArrowRight, Eye, EyeOff, KeyRound, LoaderCircle, LockKeyhole, Mail, ShieldCheck, UserRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/auth-context';
import { useToast } from '../../components/common/Toast';

const getErrorMessage = (error) => {
  const apiMessage = error.response?.data?.message;
  if (apiMessage) return apiMessage;
  if (!error.response) return 'Không kết nối được backend. Hãy kiểm tra backend và địa chỉ API.';
  return 'Không thể xác thực. Vui lòng kiểm tra thông tin và thử lại.';
};

const LoginPage = () => {
  const { login, register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isRegistering = mode === 'register';
  const updateField = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
    setErrorMessage('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage('');

    if (isRegistering && form.password.length < 8) {
      setErrorMessage('Mật khẩu cần có ít nhất 8 ký tự.');
      return;
    }
    if (new TextEncoder().encode(form.password).length > 72) {
      setErrorMessage('Mật khẩu không được dài quá 72 byte.');
      return;
    }

    setLoading(true);
    try {
      if (isRegistering) {
        await register({ fullName: form.fullName.trim(), email: form.email.trim(), password: form.password });
        toast.success('Tạo tài khoản và đăng nhập thành công.');
      } else {
        await login({ email: form.email.trim(), password: form.password });
        toast.success('Đăng nhập thành công.');
      }
      navigate('/');
    } catch (error) {
      const message = getErrorMessage(error);
      setErrorMessage(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-showcase">
        <div className="auth-brand">
          <div className="auth-brand-mark"><ShieldCheck size={21} strokeWidth={2.4} /></div>
          <span>SBA <span className="auth-brand-light">/ AUTH LAB</span></span>
        </div>

        <div className="auth-pitch">
          <div className="auth-eyebrow"><span className="auth-eyebrow-dot" /> IDENTITY & ACCESS</div>
          <h1>Kiểm thử xác thực.<br /><span>Đơn giản và trực quan.</span></h1>
          <p>Tạo tài khoản, đăng nhập và kiểm tra vòng đời JWT trực tiếp với backend SBA.</p>

          <div className="auth-flow">
            <div className="auth-flow-item">
              <div className="auth-flow-icon"><UserRound size={17} /></div>
              <div><strong>Tạo tài khoản</strong><span>Mật khẩu được băm bằng BCrypt</span></div>
              <span className="auth-flow-index">01</span>
            </div>
            <div className="auth-flow-line" />
            <div className="auth-flow-item">
              <div className="auth-flow-icon"><KeyRound size={17} /></div>
              <div><strong>Nhận access token</strong><span>JWT ký bằng HS256</span></div>
              <span className="auth-flow-index">02</span>
            </div>
            <div className="auth-flow-line" />
            <div className="auth-flow-item">
              <div className="auth-flow-icon"><LockKeyhole size={17} /></div>
              <div><strong>Thu hồi khi logout</strong><span>Token bị chặn ở request tiếp theo</span></div>
              <span className="auth-flow-index">03</span>
            </div>
          </div>
        </div>

        <div className="auth-showcase-footer">
          <span>SPRING BOOT · JWT · POSTGRESQL</span>
          <span className="auth-secure-label"><span /> API connection ready</span>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-form-wrap">
          <div className="auth-mobile-brand">
            <div className="auth-brand-mark"><ShieldCheck size={20} /></div>
            <span>SBA <span className="auth-brand-light">/ AUTH LAB</span></span>
          </div>

          <div className="auth-heading">
            <span className="auth-kicker">ACCOUNT ACCESS</span>
            <h2>{isRegistering ? 'Tạo tài khoản' : 'Chào mừng trở lại'}</h2>
            <p>{isRegistering ? 'Đăng ký tài khoản để bắt đầu kiểm thử API.' : 'Đăng nhập để kiểm tra phiên xác thực JWT.'}</p>
          </div>

          <div className="auth-tabs" role="tablist" aria-label="Chọn thao tác xác thực">
            <button
              className={mode === 'login' ? 'auth-tab active' : 'auth-tab'}
              type="button"
              role="tab"
              aria-selected={mode === 'login'}
              onClick={() => { setMode('login'); setErrorMessage(''); }}
            >
              Đăng nhập
            </button>
            <button
              className={mode === 'register' ? 'auth-tab active' : 'auth-tab'}
              type="button"
              role="tab"
              aria-selected={mode === 'register'}
              onClick={() => { setMode('register'); setErrorMessage(''); }}
            >
              Đăng ký
            </button>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            {isRegistering && (
              <label className="auth-field">
                <span>Họ và tên</span>
                <div className="auth-input-wrap">
                  <UserRound size={17} />
                  <input
                    autoComplete="name"
                    maxLength={255}
                    name="fullName"
                    onChange={updateField}
                    placeholder="Nguyễn Văn A"
                    required
                    value={form.fullName}
                  />
                </div>
              </label>
            )}

            <label className="auth-field">
              <span>Email</span>
              <div className="auth-input-wrap">
                <Mail size={17} />
                <input
                  autoComplete="email"
                  name="email"
                  onChange={updateField}
                  placeholder="you@example.com"
                  required
                  type="email"
                  value={form.email}
                />
              </div>
            </label>

            <label className="auth-field">
              <span>Mật khẩu</span>
              <div className="auth-input-wrap">
                <KeyRound size={17} />
                <input
                  autoComplete={isRegistering ? 'new-password' : 'current-password'}
                  maxLength={72}
                  minLength={isRegistering ? 8 : undefined}
                  name="password"
                  onChange={updateField}
                  placeholder={isRegistering ? 'Tối thiểu 8 ký tự' : 'Nhập mật khẩu'}
                  required
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                />
                <button
                  aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  className="auth-password-toggle"
                  onClick={() => setShowPassword((shown) => !shown)}
                  type="button"
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
              {isRegistering && <small>Mật khẩu được lưu dưới dạng BCrypt, không lưu plain text.</small>}
            </label>

            {errorMessage && <div className="auth-error" role="alert">{errorMessage}</div>}

            <button className="auth-submit" disabled={loading} type="submit">
              {loading ? <LoaderCircle className="auth-spinner" size={18} /> : null}
              {loading ? 'Đang xử lý...' : isRegistering ? 'Tạo tài khoản' : 'Đăng nhập'}
              {!loading && <ArrowRight size={17} />}
            </button>
          </form>

          <div className="auth-api-note">
            <span className="auth-api-indicator" />
            <span>API endpoint</span>
            <code>{import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080 (Vite proxy)'}</code>
          </div>
          <p className="auth-legal">Bằng việc tiếp tục, bạn đang kiểm thử dịch vụ xác thực SBA.</p>
        </div>
      </section>
    </main>
  );
};

export default LoginPage;
