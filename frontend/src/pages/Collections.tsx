import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  createCollection,
  getCollectionsByOwner,
} from '../api/client'
import type { Collection } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import ContentCard from '../components/ContentCard'

export default function Collections() {
  const { token, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [createForm, setCreateForm] = useState({ name: '', description: '' })
  const [myCollections, setMyCollections] = useState<Collection[]>([])
  const [isCreateOpen, setIsCreateOpen] = useState(false)

  const requireAuth = () => {
    if (!isAuthenticated || !token) {
      throw new Error('Authentication required.')
    }
    return token
  }

  const loadMyCollections = async () => {
    if (!isAuthenticated || !token) {
      setMyCollections([])
      return
    }
    try {
      const list = await getCollectionsByOwner(token)
      setMyCollections(list)
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  useEffect(() => {
    void loadMyCollections()
  }, [isAuthenticated, token])

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
      setStatus(`Collection created: ${created.name}`)
      setCreateForm({ name: '', description: '' })
      setIsCreateOpen(false)
      await loadMyCollections()
      navigate(`/collections/${created.id}`)
    } catch (err) {
      if (err instanceof Error) setError(err.message)
    }
  }

  return (
    <>
      <section className="hero">
        <h1>Collections</h1>
        <p>Curate mod lists you can share with the community.</p>
          <div className="action-row">
            <button
              className="button"
              type="button"
              onClick={() => {
                setError(null)
                setIsCreateOpen(true)
              }}
              disabled={!isAuthenticated}
            >
              New collection
            </button>
          </div>
          {!isAuthenticated ? <div className="empty">Login to create collections.</div> : null}
          {status ? <div className="status">{status}</div> : null}
      </section>

      <div className="content-grid">
        <section className="section-card">
          <div className="section-header">
            <h2>My collections</h2>
          </div>
          {isAuthenticated ? (
            myCollections.length === 0 ? (
              <div className="empty">No collections yet.</div>
            ) : (
              <div className="grid">
                {myCollections.map((item) => (
                  <ContentCard
                    key={item.id}
                    title={item.name}
                    description={item.description || 'No description yet.'}
                    href={`/collections/${item.id}`}
                    ariaLabel={`Open ${item.name}`}
                    media={<span>{item.name.slice(0, 2).toUpperCase()}</span>}
                  />
                ))}
              </div>
            )
          ) : (
            <div className="empty">Login to view your collections.</div>
          )}
        </section>
      </div>

      {isCreateOpen ? (
        <div className="modal-backdrop" onClick={() => setIsCreateOpen(false)}>
          <div className="modal-card" onClick={(event) => event.stopPropagation()}>
            <div className="section-header">
              <h2>Create collection</h2>
            </div>
            <form className="form" onSubmit={(event) => event.preventDefault()}>
              <label>
                Name
                <input
                  value={createForm.name}
                  onChange={(event) =>
                    setCreateForm({ ...createForm, name: event.target.value })
                  }
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
              {error ? <div className="status error">{error}</div> : null}
              <div className="modal-actions">
                <button
                  className="button ghost"
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                >
                  Cancel
                </button>
                <button
                  className="button"
                  type="button"
                  onClick={handleCreate}
                  disabled={!createForm.name.trim()}
                >
                  Create collection
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </>
  )
}
