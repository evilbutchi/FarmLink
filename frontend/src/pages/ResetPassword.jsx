import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { validateNewPassword } from '../validation'

export default function ResetPassword() {
  const [params] = useSearchParams()
  const token = params.get('token') || ''
  const navigate = useNavigate()

  const [status, setStatus] = useState('checking') // checking | valid | invalid
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Check the link as soon as the page opens so an expired or used link is reported immediately.
  useEffect(() => {
    if (!token) {
      setStatus('invalid')
      return
    }
    let cancelled = false
    api('/api/auth/reset-password/validate', { method: 'POST', body: { token } })
      .then((r) => !cancelled && setStatus(r.valid ? 'valid' : 'invalid'))
      .catch(() => !cancelled && setStatus('invalid'))
    return () => {
      cancelled = true
    }
  }, [token])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    const errs = {}
    const pwError = validateNewPassword(newPassword)
    if (pwError) errs.newPassword = pwError
    if (confirmPassword !== newPassword) errs.confirmPassword = 'Passwords do not match'
    setErrors(errs)
    if (Object.keys(errs).length) return

    setSubmitting(true)
    try {
      await api('/api/auth/reset-password', {
        method: 'POST',
        body: { token, newPassword, confirmPassword },
      })
      navigate('/login', { replace: true, state: { passwordReset: true } })
    } catch (err) {
      if (err.errors && Object.keys(err.errors).length) setErrors(err.errors)
      setError(err.message || 'Could not reset your password')
      if (err.status === 400 && !Object.keys(err.errors || {}).length) setStatus('invalid')
    } finally {
      setSubmitting(false)
    }
  }

  if (status === 'checking') {
    return <div className="center-screen">Checking your reset link…</div>
  }

  if (status === 'invalid') {
    return (
      <div className="auth-page">
        <div className="card auth-card">
          <h1>Link not valid</h1>
          <div className="alert alert-error" role="alert">
            This reset link is invalid or has expired. Please request a new one.
          </div>
          <Link className="btn btn-primary" to="/forgot-password">
            Request a new link
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Choose a new password</h1>
        <p className="muted">At least 8 characters with a letter and a number.</p>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <label>
          New password
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            autoComplete="new-password"
            className={errors.newPassword ? 'invalid' : ''}
          />
          {errors.newPassword && <span className="field-error">{errors.newPassword}</span>}
        </label>

        <label>
          Confirm new password
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            autoComplete="new-password"
            className={errors.confirmPassword ? 'invalid' : ''}
          />
          {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
        </label>

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Reset password'}
        </button>
      </form>
    </div>
  )
}
