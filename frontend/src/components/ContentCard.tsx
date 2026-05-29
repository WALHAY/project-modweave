import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'

type Props = {
  title: string
  description?: string
  href?: string
  ariaLabel?: string
  media?: ReactNode
  meta?: ReactNode
  className?: string
}

export default function ContentCard({
  title,
  description,
  href,
  ariaLabel,
  media,
  meta,
  className,
}: Props) {
  const body = (
    <>
      {media ? <div className="media">{media}</div> : null}
      <div>
        <h3>{title}</h3>
        <p>{(description.length >= 30 ? description.slice(0, 30) + "..." : description) || 'No description provided yet.'}</p>
      </div>
      {meta ? <div className="meta">{meta}</div> : null}
    </>
  )

  const classes = `card content-card${className ? ` ${className}` : ''}`

  if (!href) {
    return <article className={classes}>{body}</article>
  }

  return (
    <Link
      className={`${classes} card-link`}
      to={href}
      aria-label={ariaLabel || `Open ${title}`}
    >
      {body}
    </Link>
  )
}
