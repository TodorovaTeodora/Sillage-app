import type { MediaAsset, NotePyramid as NotePyramidType } from '../types'
import { getLocalNoteImageUrl } from '../localNoteImages'
import { MediaAttribution } from './MediaAttribution'

type Props = {
  pyramid: NotePyramidType
  compact?: boolean
  noteImages?: Record<string, MediaAsset>
}

const levels: { key: keyof NotePyramidType; label: string }[] = [
  { key: 'top', label: 'Top notes' },
  { key: 'heart', label: 'Heart notes' },
  { key: 'base', label: 'Base notes' },
]

export function NotePyramid({ pyramid, compact = false, noteImages }: Props) {
  const hasLocalImages = levels.some(({ key }) =>
    pyramid[key].some((note) => getLocalNoteImageUrl(note) !== null),
  )
  const illustrated = noteImages !== undefined || hasLocalImages
  return (
    <div
      className={`note-pyramid${compact ? ' note-pyramid--compact' : ''}${illustrated ? ' note-pyramid--illustrated' : ''}`}
    >
      {levels.map(({ key, label }) => (
        <div className={`note-tier note-tier--${key}`} key={key}>
          <span className="note-tier__label">{label}</span>
          <div className="note-tier__notes">
            {pyramid[key].length > 0
              ? pyramid[key].map((note) => {
                const image = noteImages?.[note]
                const localImageUrl = getLocalNoteImageUrl(note)
                if (!illustrated) {
                  return <span key={note}>{note}</span>
                }
                return (
                  <span className="note-card" key={note}>
                    {localImageUrl
                      ? (
                        <img
                          src={localImageUrl}
                          alt={`${note} fragrance note`}
                          loading="lazy"
                        />
                      )
                      : image
                      ? <img src={image.imageUrl} alt={image.alt} loading="lazy" />
                      : (
                        <span className="note-card__missing">
                          {noteImages === undefined ? 'Loading photo...' : 'Photo unavailable'}
                        </span>
                      )}
                    <b>{note}</b>
                    {!localImageUrl && image && <MediaAttribution asset={image} />}
                  </span>
                )
              })
              : <span className="note-tier__empty">Not published</span>}
          </div>
        </div>
      ))}
    </div>
  )
}
