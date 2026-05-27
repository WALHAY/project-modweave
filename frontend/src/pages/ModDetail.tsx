import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import {
  createComment,
  createModVersion,
  deleteComment,
  deleteMod,
  addModToCollection,
  getCollectionsByOwner,
  getCommentsForMod,
  getMod,
  getModVersions,
} from '../api/client'
import type { Collection, Comment, Mod, Version } from '../api/types'
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
  const [comments, setComments] = useState<Comment[]>([])
  const [commentText, setCommentText] = useState('')
  const [commentError, setCommentError] = useState<string | null>(null)
  const [commentStatus, setCommentStatus] = useState<string | null>(null)
  const [newVersionName, setNewVersionName] = useState('')
  const [newVersionChanges, setNewVersionChanges] = useState('')
  const [newVersionFiles, setNewVersionFiles] = useState<File[]>([])
  const [collections, setCollections] = useState<Collection[]>([])
  const [collectionSelection, setCollectionSelection] = useState('')
  const [collectionStatus, setCollectionStatus] = useState<string | null>(null)
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
      getCommentsForMod(modId, controller.signal),
    ])
      .then(([modResult, versionPage, commentList]) => {
        setMod(modResult)
        setVersions(versionPage.content)
        setComments(commentList)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message)
        }
      })
    return () => controller.abort()
  }, [modId])

  useEffect(() => {
    if (!token) {
      setCollections([])
      return
    }
    getCollectionsByOwner(token)
      .then((list) => setCollections(list))
      .catch(() => setCollections([]))
  }, [token])

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

  const refreshComments = async () => {
    if (!modId) return
    const commentList = await getCommentsForMod(modId)
    setComments(commentList)
  }

  const handleCreateComment = async () => {
    if (!modId || !token) return
    setCommentError(null)
    setCommentStatus(null)
    try {
      await createComment({ modId, content: commentText, token })
      setCommentText('')
      setCommentStatus('Comment posted.')
      await refreshComments()
    } catch (err) {
      if (err instanceof Error) setCommentError(err.message)
    }
  }

  const handleDeleteComment = async (commentId: number) => {
    if (!token) return
    setCommentError(null)
    setCommentStatus(null)
    try {
      await deleteComment({ commentId, token })
      setCommentStatus('Comment deleted.')
      await refreshComments()
    } catch (err) {
      if (err instanceof Error) setCommentError(err.message)
    }
  }

  const handleAddToCollection = async () => {
    if (!token || !modId || !collectionSelection) return
    setCollectionStatus(null)
    try {
      await addModToCollection({
        collectionId: Number(collectionSelection),
        modId,
        token,
      })
      setCollectionStatus('Added to collection.')
    } catch (err) {
      if (err instanceof Error) setCollectionStatus(err.message)
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
        <div className="action-row">
          {token ? (
            <>
              <select
                className="search-input"
                value={collectionSelection}
                onChange={(event) => setCollectionSelection(event.target.value)}
              >
                <option value="">Add to collection...</option>
                {collections.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
              <button
                className="button"
                type="button"
                onClick={handleAddToCollection}
                disabled={!collectionSelection}
              >
                Add
              </button>
              {collectionStatus ? <span className="pill">{collectionStatus}</span> : null}
            </>
          ) : (
            <span className="pill">Login to add to collection</span>
          )}
        </div>
        {canManage ? (
          <div className="action-row">
            <button className="button secondary" type="button" onClick={handleDelete}>
              Delete mod
            </button>
          </div>
        ) : null}
        {ownerError ? <div className="status error">{ownerError}</div> : null}
        {ownerStatus ? <div className="status">{ownerStatus}</div> : null}
      </section>

      {canManage ? (
        <section className="section-card">
          <div className="section-header">
            <h2>Publish new version</h2>
          </div>
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

      <section className="section-title">
        <h2>Comments</h2>
      </section>

      <section className="section-card">
        <div className="section-header">
          <h3>Share feedback</h3>
          {!token ? <span className="pill">Login to comment</span> : null}
        </div>
        <label>
          Comment
          <textarea
            value={commentText}
            onChange={(event) => setCommentText(event.target.value)}
            placeholder="Share your experience with this mod."
          />
        </label>
        <button
          className="button"
          type="button"
          onClick={handleCreateComment}
          disabled={!token || commentText.trim().length === 0}
        >
          Post comment
        </button>
        {commentError ? <div className="status error">{commentError}</div> : null}
        {commentStatus ? <div className="status">{commentStatus}</div> : null}
      </section>

      <div className="comment-list">
        {comments.length === 0 ? (
          <div className="empty">No comments yet. Be the first!</div>
        ) : (
          comments.map((comment) => {
            const canDelete =
              isAdmin || (username && comment.authorId.toLowerCase() === username.toLowerCase())
            return (
              <div className="comment-card" key={comment.id}>
                <div className="comment-meta">
                  <strong>@{comment.authorId}</strong>
                  <span>{new Date(comment.publishDate).toLocaleString()}</span>
                </div>
                <p>{comment.content}</p>
                {canDelete ? (
                  <button
                    className="button ghost"
                    type="button"
                    onClick={() => handleDeleteComment(comment.id)}
                  >
                    Delete
                  </button>
                ) : null}
              </div>
            )
          })
        )}
      </div>
    </>
  )
}
