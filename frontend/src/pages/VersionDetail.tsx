import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getFileDownloadUrl } from '../api/client'
import { getModVersions } from '../api/client'
import type { Version } from '../api/types'
import { normalizeId } from '../utils/normalize'

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
