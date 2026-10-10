import { createContext, useContext, useState, useCallback, useRef } from 'react';

const ToastContext = createContext(null);

const Toast = ({ toasts, onRemove }) => (
  <div style={{ position: 'fixed', bottom: 24, right: 24, zIndex: 9999, display: 'flex', flexDirection: 'column', gap: 10 }}>
    {toasts.map((t) => (
      <div
        key={t.id}
        style={{
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '13px 18px', borderRadius: 12,
          background: t.type === 'success' ? '#00b14f' : t.type === 'error' ? '#e53935' : '#1565c0',
          color: '#fff', fontSize: 14, fontWeight: 500,
          boxShadow: '0 4px 20px rgba(0,0,0,0.2)',
          minWidth: 280, maxWidth: 360,
          animation: 'slideIn 0.3s ease',
        }}
      >
        <span style={{ fontSize: 18 }}>
          {t.type === 'success' ? '✅' : t.type === 'error' ? '❌' : 'ℹ️'}
        </span>
        <span style={{ flex: 1 }}>{t.message}</span>
        <button
          onClick={() => onRemove(t.id)}
          style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 18, lineHeight: 1, opacity: 0.8 }}
        >×</button>
      </div>
    ))}
    <style>{`@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }`}</style>
  </div>
);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);
  const counterRef = useRef(0);

  const addToast = useCallback((message, type = 'info') => {
    const id = ++counterRef.current;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  const removeToast = useCallback((id) => setToasts((prev) => prev.filter((t) => t.id !== id)), []);

  const toast = {
    success: (msg) => addToast(msg, 'success'),
    error: (msg) => addToast(msg, 'error'),
    info: (msg) => addToast(msg, 'info'),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <Toast toasts={toasts} onRemove={removeToast} />
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
};
