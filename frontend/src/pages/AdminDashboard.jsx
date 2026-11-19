import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import UserTable from '../components/UserTable'

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/admin/dashboard').then(setData).catch((e) => setError(e.message))
  }, [])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!data) return <p className="muted">Loading dashboard…</p>

  const stats = [
    ['Total users', data.totalUsers],
    ['Farmers', data.farmers],
    ['Buyers', data.buyers],
    ['Admins', data.admins],
  ]

  return (
    <>
      <h1>Admin Dashboard</h1>
      <p className="muted">Live account statistics from the database.</p>

      <div className="stats">
        {stats.map(([label, value]) => (
          <div className="card stat" key={label}>
            <div className="stat-value">{value}</div>
            <div className="muted">{label}</div>
          </div>
        ))}
      </div>

      <section className="card">
        <div className="row-between">
          <h2>Newest accounts</h2>
          <Link to="/admin/users">View all users</Link>
        </div>
        <UserTable users={data.recentUsers} />
      </section>
    </>
  )
}
