import { useEffect, useState } from 'react'
import { api } from '../api'

const BUYER_TYPE = { HOUSEHOLD: 'Household', RESTAURANT: 'Small restaurant', STORE: 'Store' }

const UPCOMING = {
  FARMER: ['Produce listing management', 'Order tracking', 'Sales dashboard'],
  BUYER: ['Search and filter produce', 'Cart and checkout', 'Order tracking'],
}

/** Shared dashboard for FARMER and BUYER. Data comes from the role-protected backend endpoint. */
export default function ProfileDashboard({ role }) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api(role === 'FARMER' ? '/api/farmer/dashboard' : '/api/buyer/dashboard')
      .then(setData)
      .catch((e) => setError(e.message))
  }, [role])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!data) return <p className="muted">Loading dashboard…</p>

  const p = data.profile
  const rows =
    role === 'FARMER'
      ? [
          ['Farm name', p.farmName],
          ['Farm location', p.farmLocation],
          ['Email', p.email],
          ['Phone', p.phone || '—'],
        ]
      : [
          ['Buyer type', BUYER_TYPE[p.buyerType] || '—'],
          ['Delivery address', p.deliveryAddress || '—'],
          ['Email', p.email],
          ['Phone', p.phone || '—'],
        ]

  return (
    <>
      <h1>{role === 'FARMER' ? 'Farmer Dashboard' : 'Buyer Dashboard'}</h1>
      <p className="muted">{data.welcome}</p>

      <div className="grid">
        <section className="card">
          <h2>Your profile</h2>
          <dl className="details">
            {rows.map(([k, v]) => (
              <div key={k}>
                <dt>{k}</dt>
                <dd>{v}</dd>
              </div>
            ))}
            <div>
              <dt>Member since</dt>
              <dd>{new Date(p.createdAt).toLocaleDateString()}</dd>
            </div>
          </dl>
        </section>

        <section className="card">
          <h2>Coming in later increments</h2>
          <ul className="plain">
            {UPCOMING[role].map((m) => (
              <li key={m}>{m}</li>
            ))}
          </ul>
        </section>
      </div>
    </>
  )
}
