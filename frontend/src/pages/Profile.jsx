import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../AuthContext'
import { PHONE_PATTERN, validateNewPassword } from '../validation'

function toForm(u) {
  return {
    fullName: u.fullName || '',
    phone: u.phone || '',
    farmName: u.farmName || '',
    farmLocation: u.farmLocation || '',
    buyerType: u.buyerType || 'HOUSEHOLD',
    deliveryAddress: u.deliveryAddress || '',
  }
}

function validateProfile(f, role) {
  const e = {}
  if (f.fullName.trim().length < 2) e.fullName = 'Full name must be at least 2 characters'
  if (f.phone && !PHONE_PATTERN.test(f.phone)) e.phone = 'Enter a valid phone number'
  if (role === 'FARMER') {
    if (!f.farmName.trim()) e.farmName = 'Farm name is required for farmers'
    if (!f.farmLocation.trim()) e.farmLocation = 'Farm location is required for farmers'
  }
  return e
}

function ProfileForm({ user, onSaved }) {
  const [form, setForm] = useState(toForm(user))
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [saving, setSaving] = useState(false)
  const isFarmer = user.role === 'FARMER'

  const set = (name) => (e) => {
    setSuccess('')
    setForm((f) => ({ ...f, [name]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    const errs = validateProfile(form, user.role)
    setErrors(errs)
    if (Object.keys(errs).length) return

    const body = {
      fullName: form.fullName.trim(),
      phone: form.phone.trim(),
      ...(isFarmer
        ? { farmName: form.farmName.trim(), farmLocation: form.farmLocation.trim() }
        : { buyerType: form.buyerType, deliveryAddress: form.deliveryAddress.trim() }),
    }

    setSaving(true)
    try {
      const updated = await api('/api/profile', { method: 'PUT', body })
      onSaved(updated)
      setForm(toForm(updated))
      setSuccess('Profile updated.')
    } catch (err) {
      if (err.errors && Object.keys(err.errors).length) setErrors(err.errors)
      setError(err.message || 'Could not save your profile')
    } finally {
      setSaving(false)
    }
  }

  const field = (name, label, props = {}) => (
    <label>
      {label}
      <input value={form[name]} onChange={set(name)} className={errors[name] ? 'invalid' : ''} {...props} />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </label>
  )

  return (
    <form className="card form-card" onSubmit={handleSubmit} noValidate>
      <h2>Profile details</h2>

      {success && <div className="alert alert-success" role="status">{success}</div>}
      {error && <div className="alert alert-error" role="alert">{error}</div>}

      <div className="two-col">
        <label>
          Email
          <input value={user.email} readOnly />
        </label>
        <label>
          Account type
          <input value={user.role === 'FARMER' ? 'Farmer' : 'Buyer'} readOnly />
        </label>
      </div>
      <p className="hint">Email and account type cannot be changed.</p>

      {field('fullName', 'Full name', { autoComplete: 'name' })}
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

      <button className="btn btn-primary" disabled={saving}>
        {saving ? 'Saving…' : 'Save changes'}
      </button>
    </form>
  )
}

function ChangePasswordForm() {
  const empty = { currentPassword: '', newPassword: '', confirmNewPassword: '' }
  const [form, setForm] = useState(empty)
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [saving, setSaving] = useState(false)

  const set = (name) => (e) => {
    setSuccess('')
    setForm((f) => ({ ...f, [name]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    const errs = {}
    if (!form.currentPassword) errs.currentPassword = 'Enter your current password'
    const pwError = validateNewPassword(form.newPassword)
    if (pwError) errs.newPassword = pwError
    else if (form.newPassword === form.currentPassword)
      errs.newPassword = 'New password must be different from the current password'
    if (form.confirmNewPassword !== form.newPassword) errs.confirmNewPassword = 'Passwords do not match'
    setErrors(errs)
    if (Object.keys(errs).length) return

    setSaving(true)
    try {
      await api('/api/profile/password', { method: 'POST', body: form })
      setForm(empty)
      setSuccess('Your password has been changed. Use the new password next time you log in.')
    } catch (err) {
      if (err.errors && Object.keys(err.errors).length) setErrors(err.errors)
      setError(err.message || 'Could not change your password')
    } finally {
      setSaving(false)
    }
  }

  const field = (name, label, autoComplete) => (
    <label>
      {label}
      <input
        type="password"
        value={form[name]}
        onChange={set(name)}
        autoComplete={autoComplete}
        className={errors[name] ? 'invalid' : ''}
      />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </label>
  )

  return (
    <form className="card form-card" onSubmit={handleSubmit} noValidate>
      <h2>Change password</h2>

      {success && <div className="alert alert-success" role="status">{success}</div>}
      {error && <div className="alert alert-error" role="alert">{error}</div>}

      {field('currentPassword', 'Current password', 'current-password')}
      {field('newPassword', 'New password', 'new-password')}
      {field('confirmNewPassword', 'Confirm new password', 'new-password')}
      <p className="hint">At least 8 characters with a letter and a number.</p>

      <button className="btn btn-primary" disabled={saving}>
        {saving ? 'Updating…' : 'Change password'}
      </button>
    </form>
  )
}

export default function Profile() {
  const { user, updateUser } = useAuth()
  const [loaded, setLoaded] = useState(null)
  const [loadError, setLoadError] = useState('')

  // Always show the latest saved data from the server.
  useEffect(() => {
    api('/api/profile')
      .then((u) => {
        setLoaded(u)
        updateUser(u)
      })
      .catch((e) => setLoadError(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (loadError) return <div className="alert alert-error">{loadError}</div>
  if (!loaded) return <p className="muted">Loading profile…</p>

  return (
    <>
      <h1>My Profile</h1>
      <p className="muted">Manage your account details and password.</p>
      <div className="grid">
        <ProfileForm user={user || loaded} onSaved={updateUser} />
        <ChangePasswordForm />
      </div>
    </>
  )
}
