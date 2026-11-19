import { useEffect, useState } from 'react'
import { api } from '../api'
import UserTable from '../components/UserTable'

export default function AdminUsers() {
  const [users, setUsers] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/admin/users').then(setUsers).catch((e) => setError(e.message))
  }, [])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!users) return <p className="muted">Loading users…</p>

  return (
    <>
      <h1>Users</h1>
      <p className="muted">{users.length} registered account(s).</p>
      <section className="card">
        <UserTable users={users} />
      </section>
    </>
  )
}
