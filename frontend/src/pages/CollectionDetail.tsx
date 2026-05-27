import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  addModToCollection,
  deleteCollection,
  deleteModFromCollection,
  getCollection,
  getCollectionMods,
} from '../api/client'
import type { Collection, Mod } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import ModCard from '../components/ModCard'

export default function CollectionDetail() {
  const { collectionId } = useParams()
  const navigate = useNavigate()
  const { token, username, roles, isAuthenticated } = useAuth()
  const [collection, setCollection] = useState<Collection | null>(null)
  const [mods, setMods] = useState<Mod[]>([])
  const [addModId, setAddModId] = useState('')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const isAdmin = roles.includes('ROLE_ADMIN')
  const isOwner = useMemo(() => {
    if (!collection || !username) return false
    return collection.ownerId.toLowerCase() === username.toLowerCase()
  }, [collection, username])
  const canManage = isAdmin || isOwner

  const loadCollection = async () => {
    if (!collectionId) return
    setError(null)
    try {
      const [collectionResult, modsResult] = await Promise.all([
        getCollection(Number(collectionId)),
        getCollectionMods({ collectionId: Number(collectionId), page: 0, size: 50 }),
      ])
      setCollection(collectionResult)
      setMods(modsResult.content)
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  useEffect(() => {
    void loadCollection()
  }, [collectionId])

  const handleAddMod = async () => {
    if (!collectionId || !addModId || !token) return
    setStatus(null)
    setError(null)
    try {
      await addModToCollection({
        collectionId: Number(collectionId),
        modId: addModId,
        token,
      })
      setAddModId('')
      setStatus('Mod added.')
      await loadCollection()
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleMove = async (modId: string, nextIndex: number) => {
    if (!collectionId || !token) return
    setStatus(null)
    setError(null)
    try {
      await addModToCollection({
        collectionId: Number(collectionId),
        modId,
        index: nextIndex,
        token,
      })
      await loadCollection()
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleRemove = async (modId: string) => {
    if (!collectionId || !token) return
    setStatus(null)
    setError(null)
    try {
      await deleteModFromCollection({
        collectionId: Number(collectionId),
        modId,
        token,
      })
      setStatus('Mod removed.')
      await loadCollection()
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleDeleteCollection = async () => {
    if (!collectionId || !token) return
    setStatus(null)
    setError(null)
    try {
      await deleteCollection({ collectionId: Number(collectionId), token })
      navigate('/collections')
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  if (error) {
    return <div className="status error">{error}</div>
  }

  if (!collection) {
    return <div className="status">Loading collection...</div>
  }

  return (
    <>
      <section className="hero">
        <h1>{collection.name}</h1>
        <p>{collection.description || 'No description yet.'}</p>
        <div className="inline">
          <span className="pill">Owner: {collection.ownerId}</span>
          <span className="pill">Mods: {mods.length}</span>
        </div>
      </section>

      {canManage ? (
        <section className="section-card">
          <div className="section-header">
            <h2>Manage collection</h2>
          </div>
          <div className="action-row">
            <input
              className="search-input"
              placeholder="Mod ID"
              value={addModId}
              onChange={(event) => setAddModId(event.target.value)}
            />
            <button className="button" type="button" onClick={handleAddMod} disabled={!addModId}>
              Add mod
            </button>
          </div>
          <div className="action-row">
            <button className="button ghost" type="button" onClick={handleDeleteCollection}>
              Delete collection
            </button>
          </div>
        </section>
      ) : !isAuthenticated ? (
        <div className="status">Login to manage this collection.</div>
      ) : null}

      {status ? <div className="status">{status}</div> : null}

      <section className="section-title">
        <h2>Collection mods</h2>
      </section>

      {mods.length === 0 ? (
        <div className="empty">No mods in this collection yet.</div>
      ) : (
        <div className="list">
          {mods.map((mod, index) => (
            <div className="collection-item" key={mod.id}>
              <div className="collection-item-card">
                <ModCard mod={mod} />
              </div>
              {canManage ? (
                <div className="collection-item-actions">
                  <button
                    className="button ghost"
                    type="button"
                    onClick={() => handleMove(mod.id, index - 1)}
                    disabled={index === 0}
                  >
                    Move up
                  </button>
                  <button
                    className="button ghost"
                    type="button"
                    onClick={() => handleMove(mod.id, index + 1)}
                    disabled={index === mods.length - 1}
                  >
                    Move down
                  </button>
                  <button
                    className="button secondary"
                    type="button"
                    onClick={() => handleRemove(mod.id)}
                  >
                    Remove
                  </button>
                </div>
              ) : null}
            </div>
          ))}
        </div>
      )}
    </>
  )
}
