import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ROLE_HOME, useAuth } from '../AuthContext'

const EMPTY = {
  role: 'BUYER',
  fullName: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
  farmName: '',
  farmLocation: '',
  buyerType: 'HOUSEHOLD',
  deliveryAddress: '',
}

function validate(f) {
  const e = {}
  if (f.fullName.trim().length < 2) e.fullName = 'Full name must be at least 2 characters'
  if (!/^\S+@\S+\.\S+$/.test(f.email.trim())) e.email = 'Enter a valid email address'
  if (f.password.length < 8) e.password = 'Password must be at least 8 characters'
  else if (!/[A-Za-z]/.test(f.password) || !/\d/.test(f.password))
    e.password = 'Password must contain at least one letter and one number'
  if (f.confirmPassword !== f.password) e.confirmPassword = 'Passwords do not match'
  if (f.phone && !/^[0-9+()\-\s]{7,20}$/.test(f.phone)) e.phone = 'Enter a valid phone number'
  if (f.role === 'FARMER') {
    if (!f.farmName.trim()) e.farmName = 'Farm name is required for farmers'
    if (!f.farmLocation.trim()) e.farmLocation = 'Farm location is required for farmers'
  }
  return e
}

export default function Register() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to={ROLE_HOME[user.role]} replace />

  const set = (name) => (e) => setForm((f) => ({ ...f, [name]: e.target.value }))
  const isFarmer = form.role === 'FARMER'

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    const errs = validate(form)
    setErrors(errs)
    if (Object.keys(errs).length) return

    const payload = {
      role: form.role,
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      phone: form.phone.trim(),
      ...(isFarmer
        ? { farmName: form.farmName.trim(), farmLocation: form.farmLocation.trim() }
        : { buyerType: form.buyerType, deliveryAddress: form.deliveryAddress.trim() }),
    }

    setSubmitting(true)
    try {
      await register(payload)
      navigate('/login', { replace: true, state: { registered: true } })
    } catch (err) {
      // Show server-side field errors (duplicate email, validation) next to the fields.
      if (err.errors && Object.keys(err.errors).length) setErrors(err.errors)
      setError(err.message || 'Registration failed')
    } finally {
      setSubmitting(false)
    }
  }

  const field = (name, label, props = {}) => (
    <label>
      {label}
      <input
        value={form[name]}
        onChange={set(name)}
        className={errors[name] ? 'invalid' : ''}
        {...props}
      />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </label>
  )

  return (
    <div className="auth-page">
      <form className="card auth-card wide" onSubmit={handleSubmit} noValidate>
        <h1>Create your account</h1>
        <p className="muted">Sell your harvest directly, or buy fresh from local farms.</p>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="role-picker" role="radiogroup" aria-label="Account type">
          {['BUYER', 'FARMER'].map((r) => (
            <button
              type="button"
              key={r}
              className={'role-option' + (form.role === r ? ' selected' : '')}
              onClick={() => setForm((f) => ({ ...f, role: r }))}
            >
              {r === 'FARMER' ? '🌾 I am a farmer' : '🛒 I am a buyer'}
            </button>
          ))}
        </div>

        {field('fullName', 'Full name', { autoComplete: 'name' })}
        {field('email', 'Email', { type: 'email', autoComplete: 'username' })}
        <div className="two-col">
          {field('password', 'Password', { type: 'password', autoComplete: 'new-password' })}
          {field('confirmPassword', 'Confirm password', { type: 'password', autoComplete: 'new-password' })}
        </div>
        <p className="hint">At least 8 characters with a letter and a number.</p>
        {field('phone', 'Phone (optional)', { type: 'tel', autoComplete: 'tel' })}

        {isFarmer ? (
          <>
            {field('farmName', 'Farm name')}
            {field('farmLocation', 'Farm location (town / barangay)')}
          </>
        ) : (
          <>
            <label>
              I am buying as
              <select value={form.buyerType} onChange={set('buyerType')}>
                <option value="HOUSEHOLD">Household</option>
                <option value="RESTAURANT">Small restaurant</option>
                <option value="STORE">Store</option>
              </select>
              {errors.buyerType && <span className="field-error">{errors.buyerType}</span>}
            </label>
            {field('deliveryAddress', 'Delivery address (optional)')}
          </>
        )}

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>

        <p className="muted center">
          Already registered? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  )
}
