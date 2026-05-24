import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { isAuthenticated, username, roles, logout } = useAuth()
  const navigate = useNavigate()
  const isAdmin = roles.includes('ROLE_ADMIN')

  const navLinks = [
    { to: '/', label: 'Home' },
    { to: '/mods', label: 'Browse Mods' },
    { to: '/games', label: 'Browse Games' },
    ...(isAuthenticated ? [{ to: '/mods/upload', label: 'Upload Mod' }] : []),
  ]

  const adminLinks = isAdmin
    ? [
        { to: '/admin/categories', label: 'Upload Category' },
        { to: '/admin/games', label: 'Upload Game' },
      ]
    : []

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
        {adminLinks.length > 0 ? (
          <nav className="nav-group">
            <span className="pill">Admin</span>
            {adminLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `nav-link${isActive ? ' active' : ''}`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        ) : null}
      </aside>
      <div className="content">
        <header className="topbar">
          <div className="search-bar">
            <input placeholder="Search mods, creators, or games" />
          </div>
          <div className="topbar-actions">
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
