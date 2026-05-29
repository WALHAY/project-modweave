import { useEffect, useMemo, useState } from 'react'
import { getGames } from '../api/client'
import type { Game } from '../api/types'
import ContentCard from '../components/ContentCard'
import { normalizeId } from '../utils/normalize'

export default function BrowseGames() {
  const [games, setGames] = useState<Game[]>([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState<string | null>(null)

  const displayQuery = useMemo(() => query.trim(), [query])

  useEffect(() => {
    const controller = new AbortController()
    getGames({ page: 0, size: 12, name: displayQuery, signal: controller.signal })
      .then((page) => setGames(page.content))
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [displayQuery])

  return (
    <>
      <section className="section-title">
        <h2>Browse Games</h2>
        <input
          className="search-input"
          placeholder="Filter by game name"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </section>

      {error ? (
        <div className="status error">{error}</div>
      ) : games.length === 0 ? (
        <div className="empty">No games matched your search.</div>
      ) : (
        <div className="grid">
          {games.map((game) => {
            const id = normalizeId(game.id)
            const label = normalizeId(game.name) || game.name
            return (
              <ContentCard
                key={id || label}
                title={game.name}
                description={game.description || 'No description provided yet.'}
                href={id ? `/mods?gameId=${encodeURIComponent(id)}` : undefined}
                ariaLabel={`Browse mods for ${game.name}`}
                media={
                  game.imagePath ? (
                    <img src={`http://localhost:9000/images/${game.imagePath}`} alt={game.name} />
                  ) : (
                    <span>No cover yet</span>
                  )
                }
                meta={<span className="pill">{id || label || 'unknown'}</span>}
              />
            )
          })}
        </div>
      )}
    </>
  )
}
