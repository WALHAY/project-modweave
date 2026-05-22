import { useState } from 'react'
import type { FormEvent } from 'react'
import { uploadGame } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function AdminGameUpload() {
  const { token, roles } = useAuth()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [image, setImage] = useState<File | null>(null)
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
        {status ? <div className="status">{status}</div> : null}

        <button className="button" type="submit" disabled={!name.trim() || !image}>
          Upload game
        </button>
      </form>
    </section>
  )
}
