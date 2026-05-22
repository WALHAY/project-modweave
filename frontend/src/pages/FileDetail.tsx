import { useMemo } from 'react'
import { useParams } from 'react-router-dom'
import { getFileDownloadUrl } from '../api/client'

export default function FileDetail() {
  const { bucket, fileId } = useParams()

  const downloadUrl = useMemo(() => {
    if (!bucket || !fileId) {
      return null
    }
    return getFileDownloadUrl(bucket, fileId)
  }, [bucket, fileId])

  if (!bucket || !fileId) {
    return <div className="status error">Invalid file link.</div>
  }

  return (
    <section className="hero">
      <h1>File detail</h1>
      <p>Download or share the file for the selected version.</p>
      <div className="inline">
        <span className="pill">Bucket: {bucket}</span>
        <span className="pill">File ID: {fileId}</span>
      </div>
      {downloadUrl ? (
        <a className="button" href={downloadUrl}>
          Download file
        </a>
      ) : (
        <div className="status error">Unable to build download link.</div>
      )}
    </section>
  )
}
