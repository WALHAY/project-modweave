import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const navLinks = [
  { to: '/', label: 'Home' },
  { to: '/mods', label: 'Browse Mods' },
]

export default function Layout() {
  const { isAuthenticated, username, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-badge">MW</div>
          ModWeave
        </div>
        <nav className="nav-group">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `nav-link${isActive ? ' active' : ''}`
              }
              end={link.to === '/'}
            >
              {link.label}
            </NavLink>
          ))}
          <NavLink
            to={isAuthenticated && username ? `/users/${username}` : '/login'}
            className={({ isActive }) =>
              `nav-link${isActive ? ' active' : ''}`
            }
          >
            {isAuthenticated ? 'Profile' : 'Login'}
          </NavLink>
        </nav>
        <div className="sidebar-card">
          <strong>Release Train</strong>
          <span>Next drop in 3 days</span>
          <button
            className="button ghost"
            type="button"
            onClick={() => navigate('/mods')}
          >
            Explore updates
          </button>
        </div>
        <div className="sidebar-card">
          <strong>Community Signals</strong>
          <span>New mods: 48</span>
          <span>Top games: 7</span>
          <span>Creators online: 312</span>
        </div>
      </aside>
      <div className="content">
        <header className="topbar">
          <div className="search-bar">
            <span>🔎</span>
            <input placeholder="Search mods, creators, or games" />
          </div>
          <div className="topbar-actions">
            <span className="pill">Live API</span>
            {isAuthenticated ? (
              <button className="button secondary" type="button" onClick={logout}>
                Sign out
              </button>
            ) : (
              <button
                className="button secondary"
                type="button"
                onClick={() => navigate('/login')}
              >
                Sign in
              </button>
            )}
          </div>
        </header>
        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
