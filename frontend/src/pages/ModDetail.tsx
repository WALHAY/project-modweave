import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { createModVersion, deleteMod, getMod, getModVersions } from '../api/client'
import type { Mod, Version } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { normalizeId } from '../utils/normalize'

export default function ModDetail() {
  const { modId } = useParams()
  const navigate = useNavigate()
  const { token, username, roles } = useAuth()
  const [mod, setMod] = useState<Mod | null>(null)
  const [versions, setVersions] = useState<Version[]>([])
  const [error, setError] = useState<string | null>(null)
  const [ownerError, setOwnerError] = useState<string | null>(null)
  const [ownerStatus, setOwnerStatus] = useState<string | null>(null)
  const [newVersionName, setNewVersionName] = useState('')
  const [newVersionChanges, setNewVersionChanges] = useState('')
  const [newVersionFiles, setNewVersionFiles] = useState<File[]>([])
  const categories = Array.isArray(mod?.categories)
    ? mod.categories.map((category) => normalizeId(category)).filter(Boolean)
    : []
  const isAdmin = roles.includes('ROLE_ADMIN')
  const isOwner = useMemo(() => {
    if (!username || !mod) return false
    return normalizeId(mod.publisherId).toLowerCase() === username.toLowerCase()
  }, [mod, username])
  const canManage = isAdmin || isOwner

  useEffect(() => {
    if (!modId) {
      return
    }
    const controller = new AbortController()
    Promise.all([
      getMod(modId, controller.signal),
      getModVersions(modId, controller.signal),
    ])
      .then(([modResult, versionPage]) => {
        setMod(modResult)
        setVersions(versionPage.content)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [modId])

  const refreshVersions = (signal?: AbortSignal) => {
    if (!modId) return Promise.resolve()
    return getModVersions(modId, signal).then((page) => setVersions(page.content))
  }

  const handleDelete = async () => {
    if (!modId || !token) return
    setOwnerError(null)
    setOwnerStatus(null)
    try {
      await deleteMod({ modId, token })
      navigate('/mods')
    } catch (err) {
      if (err instanceof Error) {
        setOwnerError(err.message)
      }
    }
  }

  const handleCreateVersion = async () => {
    if (!modId || !token) return
    setOwnerError(null)
    setOwnerStatus(null)
    try {
      await createModVersion({
        modId,
        name: newVersionName,
        changes: newVersionChanges || undefined,
        files: newVersionFiles,
        token,
      })
      setNewVersionName('')
      setNewVersionChanges('')
      setNewVersionFiles([])
      setOwnerStatus('Version created.')
      await refreshVersions()
    } catch (err) {
      if (err instanceof Error) {
        setOwnerError(err.message)
      }
    }
  }

  if (error) {
    return <div className="status error">{error}</div>
  }

  if (!mod) {
    return <div className="status">Loading mod details...</div>
  }

  return (
    <>
      <section className="hero">
        <h1>{mod.name}</h1>
        <p>{mod.description || 'This mod is waiting for a full description.'}</p>
        <div className="inline">
          <span className="pill">Game: {normalizeId(mod.gameId)}</span>
          <span className="pill">Publisher: {normalizeId(mod.publisherId)}</span>
          <span className="pill">Created: {new Date(mod.creationDate).toDateString()}</span>
        </div>
        <div className="inline">
          {categories.length === 0 ? (
            <span className="pill">No categories</span>
          ) : (
            categories.map((name) => (
              <span className="badge" key={name}>
                {name}
              </span>
            ))
          )}
        </div>
        {canManage ? (
          <div className="inline">
            <button className="button secondary" type="button" onClick={handleDelete}>
              Delete mod
            </button>
          </div>
        ) : null}
        {ownerError ? <div className="status error">{ownerError}</div> : null}
        {ownerStatus ? <div className="status">{ownerStatus}</div> : null}
      </section>

      {canManage ? (
        <section className="form">
          <h2>New version</h2>
          <label>
            Version name
            <input
              value={newVersionName}
              onChange={(event) => setNewVersionName(event.target.value)}
              placeholder="1.0.1"
            />
          </label>
          <label>
            Changes
            <textarea
              value={newVersionChanges}
              onChange={(event) => setNewVersionChanges(event.target.value)}
              placeholder="Describe what's new."
            />
          </label>
          <label>
            Files
            <input
              type="file"
              multiple
              onChange={(event) => setNewVersionFiles(Array.from(event.target.files ?? []))}
            />
          </label>
          <button
            className="button"
            type="button"
            onClick={handleCreateVersion}
            disabled={!newVersionName.trim() || newVersionFiles.length === 0}
          >
            Publish version
          </button>
        </section>
      ) : null}

      <section className="section-title">
        <h2>Recent versions</h2>
      </section>

      <div className="list">
        {versions.length === 0 ? (
          <div className="empty">No releases yet.</div>
        ) : (
          versions.map((version) => {
            const versionId = normalizeId((version as any).id) || version.name
            return (
              <Link
                to={`/mods/${normalizeId(mod.id)}/versions/${encodeURIComponent(versionId)}`}
                className="list-item"
                key={String(versionId)}
              >
              <div>
                <strong>{version.name}</strong>
                <p>{version.changes || 'No release notes.'}</p>
              </div>
              <span className="pill">
                {new Date(version.uploadDate).toLocaleDateString()}
              </span>
              </Link>
            )
          })
        )}
      </div>
    </>
  )
}
