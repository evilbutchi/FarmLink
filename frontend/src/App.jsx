import { Navigate, Route, Routes } from 'react-router-dom'
import { ROLE_HOME, useAuth } from './AuthContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import ProfileDashboard from './pages/ProfileDashboard'
import Profile from './pages/Profile'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'

function Home() {
  const { user, loading } = useAuth()
  if (loading) return <div className="center-screen">Loading…</div>
  return <Navigate to={user ? ROLE_HOME[user.role] : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />

      {/* Authenticated area: every child route is role-restricted. */}
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route
          path="/farmer"
          element={
            <ProtectedRoute roles={['FARMER']}>
              <ProfileDashboard role="FARMER" />
            </ProtectedRoute>
          }
        />
        <Route
          path="/buyer"
          element={
            <ProtectedRoute roles={['BUYER']}>
              <ProfileDashboard role="BUYER" />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute roles={['FARMER', 'BUYER']}>
              <Profile />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route path="*" element={<Home />} />
    </Routes>
  )
}
