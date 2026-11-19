import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import Forbidden from '../pages/Forbidden'

/**
 * Guards a route. Unauthenticated visitors go to /login; authenticated users whose
 * role is not allowed see a 403 page. (The backend enforces the same rules.)
 */
export default function ProtectedRoute({ roles, children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="center-screen">Loading…</div>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (roles && !roles.includes(user.role)) return <Forbidden />
  return children
}
