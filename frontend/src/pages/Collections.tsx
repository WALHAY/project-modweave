import { useState } from 'react'
import {
  addModToCollection,
  createCollection,
  deleteCollection,
  deleteModFromCollection,
  getCollection,
  getCollectionMods,
} from '../api/client'
import type { Collection, Mod } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import ModCard from '../components/ModCard'

export default function Collections() {
  const { token, isAuthenticated } = useAuth()
  const [collectionId, setCollectionId] = useState('')
  const [collection, setCollection] = useState<Collection | null>(null)
  const [mods, setMods] = useState<Mod[]>([])
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [createForm, setCreateForm] = useState({ name: '', description: '' })
  const [addModId, setAddModId] = useState('')
  const [removeModId, setRemoveModId] = useState('')

  const requireAuth = () => {
    if (!isAuthenticated || !token) {
      throw new Error('Authentication required.')
    }
    return token
  }

  const loadCollection = async () => {
    if (!collectionId) return
    setStatus(null)
    setError(null)
    try {
      const [collectionResult, modsResult] = await Promise.all([
        getCollection(Number(collectionId)),
        getCollectionMods({ collectionId: Number(collectionId), page: 0, size: 12 }),
      ])
      setCollection(collectionResult)
      setMods(modsResult.content)
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleCreate = async () => {
    setStatus(null)
    setError(null)
    try {
      const authToken = requireAuth()
      const created = await createCollection({
        name: createForm.name,
        description: createForm.description || undefined,
        token: authToken,
      })
      setCollectionId(String(created.id))
      setCollection(created)
      setMods([])
      setStatus(`Collection created: ${created.name}`)
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleAddMod = async () => {
    if (!collectionId || !addModId) return
    setStatus(null)
    setError(null)
    try {
      const authToken = requireAuth()
      await addModToCollection({
        collectionId: Number(collectionId),
        modId: addModId,
        token: authToken,
      })
      await loadCollection()
      setStatus('Mod added to collection.')
      setAddModId('')
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleRemoveMod = async () => {
    if (!collectionId || !removeModId) return
    setStatus(null)
    setError(null)
    try {
      const authToken = requireAuth()
      await deleteModFromCollection({
        collectionId: Number(collectionId),
        modId: removeModId,
        token: authToken,
      })
      await loadCollection()
      setStatus('Mod removed from collection.')
      setRemoveModId('')
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  const handleDeleteCollection = async () => {
    if (!collectionId) return
    setStatus(null)
    setError(null)
    try {
      const authToken = requireAuth()
      await deleteCollection({ collectionId: Number(collectionId), token: authToken })
      setCollection(null)
      setMods([])
      setStatus('Collection deleted.')
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  return (
    <>
      <section className="hero">
        <h1>Collections</h1>
        <p>Curate mod lists you can share with the community.</p>
      </section>

      <div className="content-grid">
        <section className="section-card">
          <div className="section-header">
            <h2>Load collection</h2>
          </div>
          <div className="action-row">
            <input
              className="search-input"
              placeholder="Collection ID"
              value={collectionId}
              onChange={(event) => setCollectionId(event.target.value)}
            />
            <button className="button" type="button" onClick={loadCollection}>
              Load
            </button>
          </div>
          {collection ? (
            <div>
              <strong>{collection.name}</strong>
              <p className="muted">{collection.description || 'No description yet.'}</p>
              <div className="inline">
                <span className="pill">Owner: {collection.ownerId}</span>
                <span className="pill">Mods: {mods.length}</span>
              </div>
            </div>
          ) : null}
        </section>

        <section className="section-card">
          <div className="section-header">
            <h2>Create collection</h2>
          </div>
          <label>
            Name
            <input
              value={createForm.name}
              onChange={(event) => setCreateForm({ ...createForm, name: event.target.value })}
            />
          </label>
          <label>
            Description
            <textarea
              value={createForm.description}
              onChange={(event) =>
                setCreateForm({ ...createForm, description: event.target.value })
              }
            />
          </label>
          <button className="button" type="button" onClick={handleCreate}>
            Create collection
          </button>
        </section>
      </div>

      <section className="section-card">
        <div className="section-header">
          <h2>Manage collection</h2>
        </div>
        <div className="action-row">
          <input
            className="search-input"
            placeholder="Mod ID to add"
            value={addModId}
            onChange={(event) => setAddModId(event.target.value)}
          />
          <button className="button" type="button" onClick={handleAddMod}>
            Add mod
          </button>
        </div>
        <div className="action-row">
          <input
            className="search-input"
            placeholder="Mod ID to remove"
            value={removeModId}
            onChange={(event) => setRemoveModId(event.target.value)}
          />
          <button className="button secondary" type="button" onClick={handleRemoveMod}>
            Remove mod
          </button>
        </div>
        <div className="action-row">
          <button className="button ghost" type="button" onClick={handleDeleteCollection}>
            Delete collection
          </button>
        </div>
        {error ? <div className="status error">{error}</div> : null}
        {status ? <div className="status">{status}</div> : null}
      </section>

      <section className="section-title">
        <h2>Collection mods</h2>
      </section>
      {mods.length === 0 ? (
        <div className="empty">No mods in this collection yet.</div>
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
