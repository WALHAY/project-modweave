import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getUserMods, getUserProfile } from '../api/client'
import type { Mod, UserProfile } from '../api/types'
import ModCard from '../components/ModCard'

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
          {mods.map((mod) => (
            <ModCard key={mod.id} mod={mod} />
          ))}
        </div>
      )}
    </>
  )
}
