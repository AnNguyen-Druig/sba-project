import { useState } from 'react';
import { Check, Clipboard, LogOut, ShieldCheck, UserRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/auth-context';
import { useToast } from '../../components/common/Toast';

const decodeClaims = (token) => {
  try {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64), (character) => character.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch {
    return {};
  }
};

const AuthSessionPage = () => {
  const { user, accessToken, logout } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [loggingOut, setLoggingOut] = useState(false);
  const [copied, setCopied] = useState(false);
  const [showToken, setShowToken] = useState(false);
  const [logoutError, setLogoutError] = useState('');
  const claims = decodeClaims(accessToken || '');
  const expiresAt = claims.exp ? new Date(claims.exp * 1000) : null;
  const expired = expiresAt ? expiresAt.getTime() <= Date.now() : true;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(accessToken);
      setCopied(true);
      toast.success('Đã sao chép JWT vào clipboard.');
      window.setTimeout(() => setCopied(false), 1800);
    } catch {
      toast.error('Trình duyệt không cho phép sao chép clipboard.');
    }
  };

  const handleLogout = async () => {
    setLoggingOut(true);
    setLogoutError('');
    try {
      await logout();
      toast.success('Backend đã thu hồi token.');
    } catch {
      const message = 'Đã xóa phiên trên trình duyệt nhưng backend chưa xác nhận thu hồi token.';
      setLogoutError(message);
      toast.error(message);
    } finally {
      setLoggingOut(false);
      navigate('/login', { replace: true });
    }
  };

  return (
    <main className="session-shell">
      <header className="session-topbar">
        <div className="session-logo">
          <div className="auth-brand-mark"><ShieldCheck size={19} /></div>
          <span>SBA <span className="auth-brand-light">/ AUTH LAB</span></span>
        </div>
        <button className="session-logout" disabled={loggingOut} onClick={handleLogout} type="button">
          <LogOut size={15} />
          {loggingOut ? 'Đang đăng xuất...' : 'Đăng xuất'}
        </button>
      </header>

      <section className="session-content">
        <div className="session-title-row">
          <div>
            <span className="auth-kicker">AUTHENTICATION PLAYGROUND</span>
            <h1>Phiên đăng nhập</h1>
            <p>JWT đã được cấp. Bạn có thể kiểm tra claim và thử thu hồi token.</p>
          </div>
          <div className="session-live-badge"><span />{expired ? 'Token hết hạn' : 'Đã xác thực'}</div>
        </div>

        <section className="session-account-card">
          <div className="session-avatar"><UserRound size={23} /></div>
          <div className="session-account-info">
            <strong>{user?.fullName || 'Tài khoản SBA'}</strong>
            <span>{user?.email || claims.email || 'Không có email trong phiên'}</span>
          </div>
          <span className="session-role">{user?.role || claims.role || 'USER'}</span>
        </section>

        <div className="session-grid">
          <section className="session-card">
            <div className="session-card-header">
              <h2>Access token</h2>
              <span>JWT · Bearer</span>
            </div>
            <div className="session-token-preview" title={showToken ? accessToken : undefined}>
              {showToken ? accessToken : `${accessToken?.slice(0, 28)}${'•'.repeat(18)}${accessToken?.slice(-12)}`}
            </div>
            <div className="session-token-actions">
              <button className="session-secondary-button" onClick={() => setShowToken((shown) => !shown)} type="button">
                {showToken ? 'Ẩn token' : 'Hiện token'}
              </button>
              <button className="session-secondary-button" onClick={handleCopy} type="button">
                {copied ? <Check size={14} /> : <Clipboard size={14} />}
                {copied ? 'Đã sao chép' : 'Sao chép'}
              </button>
            </div>
            <dl className="session-token-claims">
              <div><dt>Subject</dt><dd>{claims.sub || user?.id || '—'}</dd></div>
              <div><dt>Role claim</dt><dd>{claims.role || user?.role || '—'}</dd></div>
              <div><dt>Token ID</dt><dd>{claims.jti || '—'}</dd></div>
              <div><dt>Hết hạn lúc</dt><dd>{expiresAt ? expiresAt.toLocaleString() : 'Không đọc được'}</dd></div>
            </dl>
          </section>

          <aside className="session-card">
            <div className="session-card-header">
              <h2>Kiểm tra logout</h2>
              <span>REVOKE</span>
            </div>
            <div className="session-tip">
              <strong>Thử thu hồi JWT</strong>
              Nhấn “Đăng xuất” để gửi access token hiện tại lên API. Token được ghi vào danh sách thu hồi và không thể dùng cho request tiếp theo.
            </div>
            <div className="session-tip" style={{ marginTop: 10 }}>
              <strong>Kiểm tra thủ công</strong>
              Sao chép token, gọi một endpoint cần đăng nhập sau khi logout và xác nhận backend trả về 401.
            </div>
            {logoutError && <div className="session-error" role="alert">{logoutError}</div>}
          </aside>
        </div>

        <p className="session-footer">TOKEN IS STORED IN THIS BROWSER · NEVER SHARE A LIVE ACCESS TOKEN</p>
      </section>
    </main>
  );
};

export default AuthSessionPage;
