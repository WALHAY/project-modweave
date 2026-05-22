import { useEffect, useMemo, useState } from 'react'
import { getMods } from '../api/client'
import type { Mod } from '../api/types'
import ModCard from '../components/ModCard'

export default function Browse() {
  const [mods, setMods] = useState<Mod[]>([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState<string | null>(null)

  const displayQuery = useMemo(() => query.trim(), [query])

  useEffect(() => {
    const controller = new AbortController()
    getMods({ page: 0, size: 12, name: displayQuery, signal: controller.signal })
      .then((page) => setMods(page.content))
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
        <h2>Browse Mods</h2>
        <input
          className="search-input"
          placeholder="Filter by mod name"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </section>

      {error ? (
        <div className="status error">{error}</div>
      ) : mods.length === 0 ? (
        <div className="empty">No mods matched your search.</div>
      ) : (
        <div className="grid">
          {mods.map((mod) => (
            <ModCard key={mod.id} mod={mod} />
          ))}
        </div>
      )}
    </>
  )
}
