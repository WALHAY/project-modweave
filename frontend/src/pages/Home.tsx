import { useEffect, useState } from 'react'
import { getMods } from '../api/client'
import type { Mod } from '../api/types'
import ContentCard from '../components/ContentCard'
import { normalizeId } from '../utils/normalize'

export default function Home() {
  const [mods, setMods] = useState<Mod[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    getMods({ page: 0, size: 6, signal: controller.signal })
      .then((page) => setMods(page.content))
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [])

  return (
    <>
      <section className="section-title">
        <h1>Trending mods</h1>
      </section>

      {error ? (
        <div className="status error">{error}</div>
      ) : (
        <div className="grid">
          {mods.length === 0 ? (
            <div className="empty">No mods published yet.</div>
          ) : (
            mods.map((mod) => {
              const modId = normalizeId(mod.id)
              const gameId = normalizeId(mod.gameId)
              const publisherId = normalizeId(mod.publisherId)
              const categoriesList = Array.isArray(mod.categories)
                ? mod.categories.map((category) => normalizeId(category)).filter(Boolean)
                : []
              return (
                <ContentCard
                  key={mod.id}
                  title={mod.name}
                  description={mod.description || 'No description provided yet.'}
                  href={modId ? `/mods/${encodeURIComponent(modId)}` : undefined}
                  media={
                    mod.imagePath ? (
                      <img src={`http://localhost:9000/images/${mod.imagePath}`} alt={mod.name} />
                    ) : (
                      <span>No preview</span>
                    )
                  }
                  meta={
                    <>
                      {gameId ? <span className="pill">{gameId}</span> : null}
                      {publisherId ? <span className="pill">{publisherId}</span> : null}
                      {categoriesList.map((category) => (
                        <span className="badge" key={category}>
                          {category}
                        </span>
                      ))}
                    </>
                  }
                />
              )
            })
          )}
        </div>
      )}
    </>
  )
}
