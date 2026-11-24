import type { MediaAsset } from '../types'

type Props = {
  asset: MediaAsset
  className?: string
}

export function MediaAttribution({ asset, className = '' }: Props) {
  return (
    <span className={`media-attribution ${className}`.trim()}>
      Image:{' '}
      <a href={asset.sourcePageUrl} target="_blank" rel="noopener noreferrer">
        {asset.author}
      </a>
      {' / '}
      <a href={asset.licenseUrl} target="_blank" rel="noopener noreferrer">
        {asset.licenseName}
      </a>
    </span>
  )
}
