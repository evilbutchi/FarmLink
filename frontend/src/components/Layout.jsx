import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

// Navigation per role. Items without a `to` belong to later increments and are shown disabled.
const MENUS = {
  FARMER: [
    { label: 'Dashboard', to: '/farmer', end: true },
    { label: 'My Listings' },
    { label: 'Orders' },
    { label: 'Sales' },
    { label: 'My Profile', to: '/profile' },
  ],
  BUYER: [
    { label: 'Dashboard', to: '/buyer', end: true },
    { label: 'Browse Produce' },
    { label: 'Cart' },
    { label: 'My Orders' },
    { label: 'My Profile', to: '/profile' },
  ],
}

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const menu = MENUS[user.role] || []

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">🌱 FarmLink</div>
        <nav className="nav">
          {menu.map((item) =>
            item.to ? (
              <NavLink
                key={item.label}
                to={item.to}
                end={item.end}
                className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
              >
                {item.label}
              </NavLink>
            ) : (
              <span key={item.label} className="nav-link disabled" title="Available in a later increment">
                {item.label} <small>soon</small>
              </span>
            )
          )}
        </nav>
        <div className="user-box">
          <span className="user-name">{user.fullName}</span>
          <span className={`badge badge-${user.role.toLowerCase()}`}>{user.role}</span>
          <button className="btn btn-ghost" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
