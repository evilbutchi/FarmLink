// Client-side checks that mirror the server rules (the server always re-validates).

export function validateNewPassword(pw) {
  if (!pw) return 'New password is required'
  if (pw.length < 8) return 'Password must be at least 8 characters'
  if (pw.length > 72) return 'Password must be at most 72 characters'
  if (!/[A-Za-z]/.test(pw) || !/\d/.test(pw)) return 'Password must contain at least one letter and one number'
  return ''
}

export const PHONE_PATTERN = /^[0-9+()\-\s]{7,20}$/
