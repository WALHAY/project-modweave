import { Link } from 'react-router-dom'
import type { Mod } from '../api/types'

type Props = {
  mod: Mod
}

export default function ModCard({ mod }: Props) {
  return (
    <article className="card">
      <div className="media">
        {mod.imagePath ? (
          <img src={mod.imagePath} alt={mod.name} />
        ) : (
          <span>No preview</span>
        )}
      </div>
      <div>
        <h3>{mod.name}</h3>
        <p>{mod.description || 'No description provided yet.'}</p>
      </div>
      <div className="meta">
        <span className="pill">{mod.gameId}</span>
        <span className="pill">{mod.publisherId}</span>
      </div>
      <Link className="button ghost" to={`/mods/${mod.id}`}>
        View details
      </Link>
    </article>
  )
}
