import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [fieldError, setFieldError] = useState('')
  const [error, setError] = useState('')
  const [sent, setSent] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (!/^\S+@\S+\.\S+$/.test(email.trim())) {
      setFieldError('Enter a valid email address')
      return
    }
    setFieldError('')
    setSubmitting(true)
    try {
      await api('/api/auth/forgot-password', { method: 'POST', body: { email: email.trim() } })
      setSent(true)
    } catch (err) {
      setError(err.errors?.email || err.message || 'Something went wrong. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Forgot your password?</h1>
        <p className="muted">Enter your account email and we will send you a link to reset it.</p>

        {sent ? (
          <>
            <div className="alert alert-success" role="status">
              If an account exists for that email, a password reset link has been sent. The link works once
              and expires soon.
            </div>
            <Link className="btn btn-primary" to="/login">
              Back to login
            </Link>
          </>
        ) : (
          <>
            {error && <div className="alert alert-error" role="alert">{error}</div>}
            <label>
              Email
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
                className={fieldError ? 'invalid' : ''}
              />
              {fieldError && <span className="field-error">{fieldError}</span>}
            </label>

            <button className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Sending…' : 'Send reset link'}
            </button>

            <p className="muted center">
              <Link to="/login">Back to login</Link>
            </p>
          </>
        )}
      </form>
    </div>
  )
}
