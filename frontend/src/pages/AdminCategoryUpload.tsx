import { useState } from 'react'
import type { FormEvent } from 'react'
import { uploadCategory } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function AdminCategoryUpload() {
  const { token, roles } = useAuth()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const isAdmin = roles.includes('ROLE_ADMIN')

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatus(null)
    setError(null)

    if (!isAdmin || !token) {
      setError('Admin access is required to create categories.')
      return
    }

    try {
      await uploadCategory({
        name,
        description: description || undefined,
        token,
      })
      setStatus('Category created successfully.')
      setName('')
      setDescription('')
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }

  return (
    <section className="hero">
      <h1>Create a category</h1>
      <p>Define categories so creators can tag their mods correctly.</p>

      <form className="form" onSubmit={onSubmit}>
        <label>
          Category name
          <input value={name} onChange={(event) => setName(event.target.value)} />
        </label>
        <label>
          Description
          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </label>

        {error ? <div className="status error">{error}</div> : null}
        {status ? <div className="status">{status}</div> : null}

        <button className="button" type="submit" disabled={!name.trim()}>
          Upload category
        </button>
      </form>
    </section>
  )
}
