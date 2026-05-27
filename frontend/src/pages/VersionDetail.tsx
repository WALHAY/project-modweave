import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { deleteModVersion, getFileDownloadUrl, getMod, getModVersion, uploadVersionFiles } from '../api/client'
import type { Mod, Version } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { normalizeId } from '../utils/normalize'

export default function VersionDetail() {
  const { modId, versionId } = useParams()
  const navigate = useNavigate()
  const { token, username, roles } = useAuth()
  const [version, setVersion] = useState<Version | null>(null)
  const [mod, setMod] = useState<Mod | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [ownerError, setOwnerError] = useState<string | null>(null)
  const [ownerStatus, setOwnerStatus] = useState<string | null>(null)
  const [uploadFiles, setUploadFiles] = useState<File[]>([])
  const isAdmin = roles.includes('ROLE_ADMIN')
  const isOwner = useMemo(() => {
    if (!username || !mod) return false
    return normalizeId(mod.publisherId).toLowerCase() === username.toLowerCase()
  }, [mod, username])
  const canManage = isAdmin || isOwner

  useEffect(() => {
    if (!modId || !versionId) return
    const controller = new AbortController()
    Promise.all([
      getMod(modId, controller.signal),
      getModVersion(modId, versionId, controller.signal),
    ])
      .then(([modResult, versionResult]) => {
        setMod(modResult)
        setVersion(versionResult)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') setError(err.message)
      })
    return () => controller.abort()
  }, [modId, versionId])

  const handleDelete = async () => {
    if (!modId || !versionId || !token) return
    setOwnerError(null)
    setOwnerStatus(null)
    try {
      await deleteModVersion({ modId, versionId, token })
      navigate(`/mods/${modId}`)
    } catch (err) {
      if (err instanceof Error) {
        setOwnerError(err.message)
      }
    }
  }

  const handleUploadFiles = async () => {
    if (!modId || !versionId || !token || uploadFiles.length === 0) return
    setOwnerError(null)
    setOwnerStatus(null)
    try {
      const next = await uploadVersionFiles({ modId, versionId, files: uploadFiles, token })
      setVersion(next)
      setUploadFiles([])
      setOwnerStatus('Files uploaded.')
    } catch (err) {
      if (err instanceof Error) {
        setOwnerError(err.message)
      }
    }
  }

  if (error) return <div className="status error">{error}</div>
  if (!version) return <div className="status">Loading version...</div>

  const files = Array.isArray(version.files) ? version.files : []

  return (
    <>
      <section className="section-title">
        <h2>Version: {version.name}</h2>
        <div className="inline">
          <span className="pill">Uploaded: {new Date((version as any).uploadDate).toLocaleString()}</span>
          <Link to={`/mods/${modId}`} className="pill">Back to mod</Link>
        </div>
      </section>

      {canManage ? (
        <section className="form">
          <h3>Owner actions</h3>
          <label>
            Upload files to this version
            <input
              type="file"
              multiple
              onChange={(event) => setUploadFiles(Array.from(event.target.files ?? []))}
            />
          </label>
          <div className="inline">
            <button
              className="button"
              type="button"
              onClick={handleUploadFiles}
              disabled={uploadFiles.length === 0}
            >
              Upload files
            </button>
            <button className="button secondary" type="button" onClick={handleDelete}>
              Delete version
            </button>
          </div>
          {ownerError ? <div className="status error">{ownerError}</div> : null}
          {ownerStatus ? <div className="status">{ownerStatus}</div> : null}
        </section>
      ) : null}

      <section>
        <h3>Release notes</h3>
        <p>{version.changes || 'No release notes.'}</p>
      </section>

      <section className="section-title">
        <h3>Files</h3>
      </section>

      <div className="list">
        {files.length === 0 ? (
          <div className="empty">No files attached to this release.</div>
        ) : (
          files.map((file) => {
            const fileId = normalizeId(file.id)
            const filename = file.filename
            return (
              <div className="list-item" key={String(fileId)}>
                <div>
                  <strong>{filename}</strong>
                  <div className="muted">{file.filePath}</div>
                </div>
                <div className="inline">
                  <a
                    className="button small"
                    href={getFileDownloadUrl('mods', fileId)}
                  >
                    Download
                  </a>
                </div>
              </div>
            )
          })
        )}
      </div>
    </>
  )
}
