import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getCategories, getGames, uploadMod } from '../api/client'
import type { Category, Game } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { normalizeId } from '../utils/normalize'

type FormState = {
  name: string
  description: string
  versionName: string
  gameId: string
  categories: Set<string>
  image: File | null
  files: File[]
}

const initialState: FormState = {
  name: '',
  description: '',
  versionName: '',
  gameId: '',
  categories: new Set(),
  image: null,
  files: [],
}

export default function ModUpload() {
  const { isAuthenticated, token } = useAuth()
  const [form, setForm] = useState<FormState>(initialState)
  const [categories, setCategories] = useState<Category[]>([])
  const [games, setGames] = useState<Game[]>([])
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const canSubmit = useMemo(() => {
    return (
      form.name.trim().length >= 3 &&
      form.versionName.trim().length > 0 &&
      form.gameId.length > 0 &&
      form.image &&
      form.files.length > 0
    )
  }, [form])

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      getCategories(controller.signal),
      getGames({ page: 0, size: 50, signal: controller.signal }),
    ])
      .then(([categoryList, gamePage]) => {
        setCategories(categoryList)
        setGames(gamePage.content)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [])

  const toggleCategory = (name: string) => {
    setForm((prev) => {
      const next = new Set(prev.categories)
      if (next.has(name)) {
        next.delete(name)
      } else {
        next.add(name)
      }
      return { ...prev, categories: next }
    })
  }

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatus(null)
    setError(null)

    if (!isAuthenticated || !token) {
      setError('You must be signed in to upload mods.')
      return
    }
    if (!form.image) {
      setError('Please select a preview image.')
      return
    }
    if (form.files.length === 0) {
      setError('Please add at least one mod file.')
      return
    }

    try {
      await uploadMod({
        name: form.name,
        description: form.description || undefined,
        image: form.image,
        categories: Array.from(form.categories),
        versionName: form.versionName,
        files: form.files,
        gameId: form.gameId,
        token,
      })
      setStatus('Mod uploaded successfully.')
      setForm(initialState)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }

  return (
    <section className="hero">
      <h1>Upload a new mod</h1>
      <p>Publish a mod with its first version and attach the required files.</p>

      <form className="form" onSubmit={onSubmit}>
        <label>
          Mod name
          <input
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            placeholder="Crystal Realism Pack"
          />
        </label>
        <label>
          Description
          <textarea
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
            placeholder="Describe what your mod adds."
          />
        </label>
        <label>
          Game
          <select
            value={form.gameId}
            onChange={(event) => setForm({ ...form, gameId: event.target.value })}
          >
            <option value="">Select a game</option>
            {games.map((game) => (
              <option key={normalizeId(game.id)} value={normalizeId(game.id)}>
                {game.name}
              </option>
            ))}
          </select>
        </label>
        <div className="checkbox-grid">
          <span>Categories</span>
          {categories.length === 0 ? (
            <span className="pill">No categories yet</span>
          ) : (
            <div className="badge-list">
              {categories.map((category) => {
                const name = normalizeId(category.name)
                const selected = form.categories.has(name)
                return (
                  <button
                    key={name}
                    type="button"
                    className={`badge selectable ${selected ? 'selected' : ''}`}
                    onClick={() => toggleCategory(name)}
                    aria-pressed={selected}
                  >
                    {name}
                  </button>
                )
              })}
            </div>
          )}
        </div>
        <label>
          Preview image
          <input
            type="file"
            accept="image/*"
            onChange={(event) =>
              setForm({
                ...form,
                image: event.target.files?.[0] ?? null,
              })
            }
          />
        </label>
        <label>
          First version name
          <input
            value={form.versionName}
            onChange={(event) =>
              setForm({ ...form, versionName: event.target.value })
            }
            placeholder="1.0.0"
          />
        </label>
        <label>
          Mod files
          <input
            type="file"
            multiple
            onChange={(event) =>
              setForm({
                ...form,
                files: Array.from(event.target.files ?? []),
              })
            }
          />
        </label>

        {error ? <div className="status error">{error}</div> : null}
        {status ? <div className="status">{status}</div> : null}

        <button className="button" type="submit" disabled={!canSubmit}>
          Upload mod
        </button>
      </form>
    </section>
  )
}
