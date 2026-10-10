/**
 * Format a number as Vietnamese currency (VND)
 * @param {number} amount
 * @returns {string} e.g. "2.500.000 ₫"
 */
export const formatCurrency = (amount) => {
  if (amount == null || isNaN(amount)) return '—';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

/**
 * Format currency without symbol, just the number with dots
 * @param {number} amount
 * @returns {string} e.g. "2.500.000"
 */
export const formatNumber = (amount) => {
  if (amount == null || isNaN(amount)) return '—';
  return new Intl.NumberFormat('vi-VN').format(amount);
};

/**
 * Format a date string to Vietnamese format
 * @param {string|Date} date
 * @returns {string} e.g. "09/10/2026"
 */
export const formatDate = (date) => {
  if (!date) return '—';
  return new Intl.DateTimeFormat('vi-VN').format(new Date(date));
};

/**
 * Format datetime to Vietnamese locale
 * @param {string|Date} date
 * @returns {string} e.g. "09/10/2026, 18:30"
 */
export const formatDateTime = (date) => {
  if (!date) return '—';
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(date));
};

/**
 * Format price per month
 * @param {number} amount
 * @returns {string} e.g. "2.500.000 ₫/tháng"
 */
export const formatPricePerMonth = (amount) => {
  if (amount == null || isNaN(amount)) return '—';
  return `${formatCurrency(amount)}/tháng`;
};

/**
 * Truncate a string to max length with ellipsis
 * @param {string} str
 * @param {number} maxLength
 * @returns {string}
 */
export const truncate = (str, maxLength = 50) => {
  if (!str) return '';
  return str.length > maxLength ? `${str.substring(0, maxLength)}...` : str;
};

/**
 * Map status code to Vietnamese label
 * @param {string} status
 * @returns {string}
 */
export const formatStatus = (status) => {
  const map = {
    ACTIVE: 'Hoạt động',
    INACTIVE: 'Ngừng hoạt động',
    PENDING: 'Chờ duyệt',
    LOCKED: 'Đã khoá',
    OCCUPIED: 'Đã thuê',
    VACANT: 'Còn trống',
    AVAILABLE: 'Còn trống',
  };
  return map[status] || status;
};
