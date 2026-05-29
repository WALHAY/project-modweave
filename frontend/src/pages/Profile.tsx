import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getUserMods, getUserProfile } from '../api/client'
import type { Mod, UserProfile } from '../api/types'
import ContentCard from '../components/ContentCard'
import { normalizeId } from '../utils/normalize'

export default function Profile() {
  const { username } = useParams()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [mods, setMods] = useState<Mod[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!username) {
      return
    }
    const controller = new AbortController()
    Promise.all([
      getUserProfile(username, controller.signal),
      getUserMods(username, controller.signal),
    ])
      .then(([profileResult, modsResult]) => {
        setProfile(profileResult)
        setMods(modsResult.content)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [username])

  if (error) {
    return <div className="status error">{error}</div>
  }

  if (!profile || !username) {
    return <div className="status">Loading profile...</div>
  }

  return (
    <>
      <section className="hero">
        <h1>{profile.name}</h1>
        <p>Creator profile for @{username}</p>
        <div className="inline">
          <span className="pill">
            Joined {new Date(profile.registerDate).toLocaleDateString()}
          </span>
          <span className="pill">Mods published: {mods.length}</span>
        </div>
      </section>

      <section className="section-title">
        <h2>Published mods</h2>
      </section>

      {mods.length === 0 ? (
        <div className="empty">No mods published yet.</div>
      ) : (
        <div className="grid">
          {mods.map((mod) => {
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
          })}
        </div>
      )}
    </>
  )
}
