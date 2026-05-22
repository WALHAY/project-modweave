import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getModVersions, getFileDownloadUrl } from '../api/client'
import type { Version } from '../api/types'

export default function VersionDetail() {
  const { modId, versionName } = useParams()
  const [version, setVersion] = useState<Version | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!modId || !versionName) return
    const controller = new AbortController()
    getModVersions(modId, controller.signal)
      .then((page) => {
        // try to find by name or id
        const found = page.content.find(
          (v) => (v as any).name === versionName || (v as any).id === versionName
        )
        if (!found) {
          setError('Version not found')
          return
        }
        setVersion(found)
      })
      .catch((err) => {
        if (err instanceof Error && err.name !== 'AbortError') setError(err.message)
      })
    return () => controller.abort()
  }, [modId, versionName])

  if (error) return <div className="status error">{error}</div>
  if (!version) return <div className="status">Loading version...</div>

  // defensive file list handling
  const files = Array.isArray((version as any).files) ? (version as any).files : []
  const bucket = (version as any).bucket ?? 'default'

  return (
    <>
      <section className="section-title">
        <h2>Version: {version.name}</h2>
        <div className="inline">
          <span className="pill">Uploaded: {new Date((version as any).uploadDate).toLocaleString()}</span>
          <Link to={`/mods/${modId}`} className="pill">Back to mod</Link>
        </div>
      </section>

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
          files.map((file: any) => {
            const fileId = file.id ?? file.fileId ?? file.name
            const filename = file.name ?? String(fileId)
            const size = file.size ? ` (${Math.round(file.size / 1024)} KB)` : ''
            const url = getFileDownloadUrl(bucket, fileId)
            return (
              <div className="list-item" key={String(fileId)}>
                <div>
                  <strong>{filename}</strong>
                  <div className="muted">{file.description || ''}</div>
                </div>
                <div className="inline">
                  <a className="button small" href={url} target="_blank" rel="noreferrer">Download{size}</a>
                </div>
              </div>
            )
          })
        )}
      </div>
    </>
  )
}
