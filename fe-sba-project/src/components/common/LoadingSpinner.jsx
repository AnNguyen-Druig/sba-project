const LoadingSpinner = ({ fullScreen = false, size = 40, message = 'Đang tải...' }) => {
  const spinnerStyle = {
    width: size,
    height: size,
    border: `4px solid #e0e0e0`,
    borderTop: `4px solid #00b14f`,
    borderRadius: '50%',
    animation: 'spin 0.8s linear infinite',
  };

  const wrapperStyle = fullScreen
    ? {
        position: 'fixed',
        inset: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'rgba(255,255,255,0.85)',
        zIndex: 9999,
        gap: 12,
      }
    : {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 32,
        gap: 12,
      };

  return (
    <div style={wrapperStyle}>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      <div style={spinnerStyle} />
      {message && <p style={{ color: '#666', fontSize: 14, margin: 0 }}>{message}</p>}
    </div>
  );
};

export default LoadingSpinner;
