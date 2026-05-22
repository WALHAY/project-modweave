import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getMod, getModVersions } from '../api/client'
import type { Mod, Version } from '../api/types'

export default function ModDetail() {
  const { modId } = useParams()
  const [mod, setMod] = useState<Mod | null>(null)
  const [versions, setVersions] = useState<Version[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!modId) {
      return
    }
    const controller = new AbortController()
    Promise.all([
      getMod(modId, controller.signal),
      getModVersions(modId, controller.signal),
    ])
      .then(([modResult, versionPage]) => {
        setMod(modResult)
        setVersions(versionPage.content)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [modId])

  if (error) {
    return <div className="status error">{error}</div>
  }

  if (!mod) {
    return <div className="status">Loading mod details...</div>
  }

  return (
    <>
      <section className="hero">
        <h1>{mod.name}</h1>
        <p>{mod.description || 'This mod is waiting for a full description.'}</p>
        <div className="inline">
          <span className="pill">Game: {mod.gameId}</span>
          <span className="pill">Publisher: {mod.publisherId}</span>
          <span className="pill">Created: {new Date(mod.creationDate).toDateString()}</span>
        </div>
      </section>

      <section className="section-title">
        <h2>Recent versions</h2>
      </section>

      <div className="list">
        {versions.length === 0 ? (
          <div className="empty">No releases yet.</div>
        ) : (
          versions.map((version) => (
            <div className="list-item" key={version.name}>
              <div>
                <strong>{version.name}</strong>
                <p>{version.changes || 'No release notes.'}</p>
              </div>
              <span className="pill">
                {new Date(version.uploadDate).toLocaleDateString()}
              </span>
            </div>
          ))
        )}
      </div>
    </>
  )
}
