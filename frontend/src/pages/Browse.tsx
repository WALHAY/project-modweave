import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getCategories, getGames, getMods } from '../api/client'
import type { Category, Game, Mod } from '../api/types'
import ModCard from '../components/ModCard'
import { normalizeId } from '../utils/normalize'

export default function Browse() {
  const [mods, setMods] = useState<Mod[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [games, setGames] = useState<Game[]>([])
  const [query, setQuery] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<Set<string>>(new Set())
  const [selectedGame, setSelectedGame] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [searchParams] = useSearchParams()

  const displayQuery = useMemo(() => query.trim(), [query])
  const filteredMods = useMemo(() => {
    return mods.filter((mod) => {
      const modGameId = normalizeId(mod.gameId)
      const modCategories = mod.categories.map((category) => normalizeId(category))
      const selectedGameId = selectedGame.trim().toLowerCase()
      if (selectedGameId && modGameId.toLowerCase() !== selectedGameId) {
        return false
      }
      // if no categories selected, accept all; otherwise ensure mod has at least one selected category
      if (selectedCategories.size > 0) {
        const hasAny = modCategories.some((category) =>
          Array.from(selectedCategories).some((sel) => category.toLowerCase() === sel.toLowerCase())
        )
        if (!hasAny) return false
      }
      return true
    })
  }, [mods, selectedCategories, selectedGame])

  useEffect(() => {
    const controller = new AbortController()
    getMods({ page: 0, size: 12, name: displayQuery, signal: controller.signal })
      .then((page) => setMods(page.content))
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [displayQuery])

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      getCategories(controller.signal),
      getGames({ page: 0, size: 50, signal: controller.signal }),
    ])
      .then(([categoryList, gamePage]) => {
        setCategories(categoryList)
        setGames(gamePage.content)
        const gameFilter = searchParams.get('gameId')
        if (gameFilter) {
          setSelectedGame(gameFilter)
        }
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [searchParams])

  return (
    <>
      <section className="section-title">
        <h2>Browse Mods</h2>
      </section>

      <section className="section-card">
        <div className="section-header">
          <strong>Filters</strong>
        </div>
        <div className="filters">
          <input
            className="search-input"
            placeholder="Filter by mod name"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <select
            className="search-input"
            value={selectedGame}
            onChange={(event) => setSelectedGame(event.target.value)}
          >
            <option value="">All games</option>
            {games.map((game) => {
              const id = normalizeId(game.id)
              return (
                <option key={id} value={id}>
                  {game.name}
                </option>
              )
            })}
          </select>
        </div>
        <div className="badge-list" role="list">
          <button
            type="button"
            className={`badge selectable ${selectedCategories.size === 0 ? 'selected' : ''}`}
            onClick={() => setSelectedCategories(new Set())}
            aria-pressed={selectedCategories.size === 0}
          >
            All
          </button>
          {categories.map((category) => {
            const name = normalizeId(category.name)
            const selected = selectedCategories.has(name)
            return (
              <button
                key={name}
                type="button"
                className={`badge selectable ${selected ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedCategories((prev) => {
                    const next = new Set(prev)
                    if (next.has(name)) next.delete(name)
                    else next.add(name)
                    return next
                  })
                }}
                aria-pressed={selected}
                role="listitem"
              >
                {name}
              </button>
            )
          })}
        </div>
      </section>

      {error ? (
        <div className="status error">{error}</div>
      ) : filteredMods.length === 0 ? (
        <div className="empty">No mods matched your search.</div>
      ) : (
        <div className="grid">
          {filteredMods.map((mod) => (
            <ModCard key={mod.id} mod={mod} />
          ))}
        </div>
      )}
    </>
  )
}
