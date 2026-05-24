import { useEffect, useState } from 'react'
import { getMods } from '../api/client'
import type { Mod } from '../api/types'
import ModCard from '../components/ModCard'

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
            mods.map((mod) => <ModCard key={mod.id} mod={mod} />)
          )}
        </div>
      )}
    </>
  )
}
