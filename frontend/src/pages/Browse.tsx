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
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedGame, setSelectedGame] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [searchParams] = useSearchParams()

  const displayQuery = useMemo(() => query.trim(), [query])
  const filteredMods = useMemo(() => {
    return mods.filter((mod) => {
      const modGameId = normalizeId(mod.gameId)
      const modCategories = mod.categories.map((category) => normalizeId(category))
      const selectedGameId = selectedGame.trim().toLowerCase()
      const selectedCategoryId = selectedCategory.trim().toLowerCase()
      if (selectedGameId && modGameId.toLowerCase() !== selectedGameId) {
        return false
      }
      if (
        selectedCategoryId &&
        !modCategories.some((category) => category.toLowerCase() === selectedCategoryId)
      ) {
        return false
      }
      return true
    })
  }, [mods, selectedCategory, selectedGame])

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
          <select
            className="search-input"
            value={selectedCategory}
            onChange={(event) => setSelectedCategory(event.target.value)}
          >
            <option value="">All categories</option>
            {categories.map((category) => {
              const label = category.name
              return (
                <option key={label} value={label}>
                  {label}
                </option>
              )
            })}
          </select>
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
