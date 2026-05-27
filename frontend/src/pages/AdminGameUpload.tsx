import { useState } from 'react'
import type { FormEvent } from 'react'
import { uploadGame } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function AdminGameUpload() {
  const { token, roles } = useAuth()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [image, setImage] = useState<File | null>(null)
  const [isOpen, setIsOpen] = useState(false)
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const isAdmin = roles.includes('ROLE_ADMIN')

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatus(null)
    setError(null)

    if (!isAdmin || !token) {
      setError('Admin access is required to create games.')
      return
    }
    if (!image) {
      setError('Please select a cover image.')
      return
    }

    try {
      await uploadGame({
        name,
        description: description || undefined,
        image,
        token,
      })
      setStatus('Game created successfully.')
      setName('')
      setDescription('')
      setImage(null)
      setIsOpen(false)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }

  return (
    <section className="hero">
      <h1>Create a game</h1>
      <p>Upload a game so users can publish mods for it.</p>

      <div className="action-row">
        <button
          className="button"
          type="button"
          onClick={() => {
            setError(null)
            setIsOpen(true)
          }}
        >
          New game
        </button>
      </div>

      {status ? <div className="status">{status}</div> : null}

      {isOpen ? (
        <div className="modal-backdrop" onClick={() => setIsOpen(false)}>
          <div className="modal-card" onClick={(event) => event.stopPropagation()}>
            <div className="section-header">
              <h2>Create game</h2>
            </div>
            <form className="form" onSubmit={onSubmit}>
              <label>
                Game name
                <input value={name} onChange={(event) => setName(event.target.value)} />
              </label>
              <label>
                Description
                <textarea
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />
              </label>
              <label>
                Cover image
                <input
                  type="file"
                  accept="image/*"
                  onChange={(event) => setImage(event.target.files?.[0] ?? null)}
                />
              </label>

              {error ? <div className="status error">{error}</div> : null}

              <div className="modal-actions">
                <button
                  className="button ghost"
                  type="button"
                  onClick={() => setIsOpen(false)}
                >
                  Cancel
                </button>
                <button className="button" type="submit" disabled={!name.trim() || !image}>
                  Upload game
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}
