import { Link } from 'react-router-dom'
import type { Mod } from '../api/types'
import { normalizeId } from '../utils/normalize'

type Props = {
  mod: Mod
}

export default function ModCard({ mod }: Props) {
  const modId = normalizeId(mod.id)
  const gameId = normalizeId(mod.gameId)
  const publisherId = normalizeId(mod.publisherId)
  const categories = Array.isArray(mod.categories)
    ? mod.categories.map((category) => normalizeId(category)).filter(Boolean)
    : []

  const cardBody = (
    <>
      <div className="media">
        {mod.imagePath ? (
          <img src={`http://localhost:9000/images/${mod.imagePath}`} alt={mod.name} />
        ) : (
          <span>No preview</span>
        )}
      </div>
      <div>
        <h3>{mod.name}</h3>
        <p>{mod.description || 'No description provided yet.'}</p>
      </div>
      <div className="meta">
        {gameId ? <span className="pill">{gameId}</span> : null}
        {publisherId ? <span className="pill">{publisherId}</span> : null}
        {categories.map((category) => (
          <span className="badge" key={category}>
            {category}
          </span>
        ))}
      </div>
    </>
  )

  if (!modId) {
    return <article className="card">{cardBody}</article>
  }

  return (
    <Link
      className="card card-link"
      to={`/mods/${encodeURIComponent(modId)}`}
      aria-label={`Open ${mod.name}`}
    >
      {cardBody}
    </Link>
  )
}
