import { Link } from 'react-router-dom'
import { ROLE_HOME, useAuth } from '../AuthContext'

export default function Forbidden() {
  const { user } = useAuth()
  return (
    <div className="center-screen">
      <div className="card" style={{ maxWidth: 420, textAlign: 'center' }}>
        <h1>403</h1>
        <p>You do not have permission to view this page.</p>
        {user && (
          <Link className="btn btn-primary" to={ROLE_HOME[user.role]}>
            Go to my dashboard
          </Link>
        )}
      </div>
    </div>
  )
}
