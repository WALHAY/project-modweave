import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <section className="card">
      <h3>Page not found</h3>
      <p>The page you requested is not available.</p>
      <Link className="button ghost" to="/">
        Return home
      </Link>
    </section>
  )
}
