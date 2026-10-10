/**
 * Validate email address
 * @param {string} email
 * @returns {boolean}
 */
export const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

/**
 * Validate Vietnamese phone number
 * @param {string} phone
 * @returns {boolean}
 */
export const isValidPhone = (phone) => {
  return /^(0|\+84)(3[2-9]|5[25689]|7[06789]|8[1-9]|9[0-9])\d{7}$/.test(phone);
};

/**
 * Validate password strength (min 8 chars, 1 uppercase, 1 number)
 * @param {string} password
 * @returns {{ valid: boolean, message: string }}
 */
export const validatePassword = (password) => {
  if (!password || password.length < 8) {
    return { valid: false, message: 'Mật khẩu phải có ít nhất 8 ký tự' };
  }
  if (!/[A-Z]/.test(password)) {
    return { valid: false, message: 'Mật khẩu phải có ít nhất 1 chữ hoa' };
  }
  if (!/\d/.test(password)) {
    return { valid: false, message: 'Mật khẩu phải có ít nhất 1 chữ số' };
  }
  return { valid: true, message: '' };
};

/**
 * Validate that two passwords match
 * @param {string} password
 * @param {string} confirmPassword
 * @returns {boolean}
 */
export const passwordsMatch = (password, confirmPassword) => password === confirmPassword;

/**
 * Validate required field is not empty
 * @param {string} value
 * @returns {boolean}
 */
export const isRequired = (value) => value !== null && value !== undefined && value.toString().trim() !== '';

/**
 * Validate bank account number (6-19 digits)
 * @param {string} accountNumber
 * @returns {boolean}
 */
export const isValidBankAccount = (accountNumber) => {
  return /^\d{6,19}$/.test(accountNumber);
};

/**
 * Validate a full form object and return errors
 * @param {object} values
 * @param {object} rules - { fieldName: [{ validator, message }] }
 * @returns {object} errors
 */
export const validateForm = (values, rules) => {
  const errors = {};
  Object.entries(rules).forEach(([field, fieldRules]) => {
    for (const rule of fieldRules) {
      if (!rule.validator(values[field])) {
        errors[field] = rule.message;
        break;
      }
    }
  });
  return errors;
};
