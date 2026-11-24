import { useId } from 'react'
import type { Fragrance } from '../types'

type ArtworkSize = 'card' | 'display' | 'large' | 'shelf'

type GeneratedBottleArtworkProps = {
  fragrance: Fragrance
  size?: ArtworkSize
  caption?: boolean
  scene?: boolean
}

type BottleDesign = {
  background: string
  cap: number
  decoration: number
  glass: string
  glassEdge: string
  label: string
  labelInk: string
  liquid: string
  metal: string
  shape: number
}

const bottleShapes = [
  'M54 91 Q54 76 70 70 L88 63 L152 63 L170 70 Q186 76 186 91 L186 236 Q186 251 171 251 L69 251 Q54 251 54 236 Z',
  'M72 72 Q57 75 51 94 Q44 117 47 176 Q49 229 67 246 Q82 258 120 258 Q158 258 173 246 Q191 229 193 176 Q196 117 189 94 Q183 75 168 72 Z',
  'M79 66 L161 66 Q174 66 178 79 L189 231 Q191 251 171 254 L69 254 Q49 251 51 231 L62 79 Q66 66 79 66 Z',
  'M67 82 L86 62 L154 62 L173 82 L192 226 L170 253 L70 253 L48 226 Z',
  'M43 112 Q43 91 65 84 L91 75 L149 75 L175 84 Q197 91 197 112 L190 229 Q189 252 166 254 L74 254 Q51 252 50 229 Z',
  'M91 67 L149 67 L155 82 Q158 94 176 104 Q193 114 193 136 L187 230 Q186 252 164 255 L76 255 Q54 252 53 230 L47 136 Q47 114 64 104 Q82 94 85 82 Z',
  'M59 91 L82 68 L158 68 L181 91 L191 222 L168 254 L72 254 L49 222 Z',
  'M62 105 Q62 86 82 79 Q98 73 101 62 L139 62 Q142 73 158 79 Q178 86 178 105 L188 231 Q190 253 168 255 L72 255 Q50 253 52 231 Z',
]

function hashString(value: string) {
  let hash = 2166136261
  for (const character of value) {
    hash ^= character.charCodeAt(0)
    hash = Math.imul(hash, 16777619)
  }
  return hash >>> 0
}

function hsl(hue: number, saturation: number, lightness: number) {
  return `hsl(${hue} ${saturation}% ${lightness}%)`
}

function createDesign(fragrance: Fragrance): BottleDesign {
  const seed = hashString([
    fragrance.id,
    fragrance.name,
    fragrance.manufacturer,
    fragrance.family,
    fragrance.pyramid.top.join(','),
  ].join('|'))
  const hue = seed % 360
  const darkBottle = seed % 7 === 0
  const paleBottle = seed % 5 === 0
  const saturation = 24 + ((seed >>> 8) % 38)

  return {
    background: hsl((hue + 24) % 360, 20, 92 + ((seed >>> 18) % 4)),
    cap: (seed >>> 4) % 6,
    decoration: (seed >>> 15) % 7,
    glass: darkBottle
      ? hsl(hue, saturation, 23)
      : hsl(hue, paleBottle ? 20 : saturation, paleBottle ? 82 : 58),
    glassEdge: hsl(hue, Math.min(saturation + 12, 72), darkBottle ? 14 : 39),
    label: seed % 4 === 0 ? hsl((hue + 180) % 360, 18, 19) : hsl((hue + 38) % 360, 24, 94),
    labelInk: seed % 4 === 0 ? '#f5efe4' : hsl((hue + 190) % 360, 22, 20),
    liquid: hsl((hue + 16) % 360, Math.min(saturation + 16, 78), darkBottle ? 34 : 48),
    metal: seed % 3 === 0 ? '#c8a562' : seed % 3 === 1 ? '#353a39' : '#c5c7c3',
    shape: seed % bottleShapes.length,
  }
}

function splitName(name: string) {
  const words = name.trim().split(/\s+/)
  if (name.length <= 18 || words.length === 1) {
    return [name.length > 22 ? `${name.slice(0, 20)}…` : name]
  }

  let first = ''
  let second = ''
  for (const word of words) {
    if (!second && `${first} ${word}`.trim().length <= 16) {
      first = `${first} ${word}`.trim()
    } else {
      second = `${second} ${word}`.trim()
    }
  }

  return [first, second.length > 18 ? `${second.slice(0, 16)}…` : second].filter(Boolean)
}

function Cap({ design }: { design: BottleDesign }) {
  const common = { fill: design.metal, stroke: '#1e2422', strokeOpacity: 0.3 }
  switch (design.cap) {
    case 0:
      return <rect x="91" y="34" width="58" height="38" rx="3" {...common} />
    case 1:
      return (
        <>
          <rect x="103" y="51" width="34" height="22" rx="3" {...common} />
          <circle cx="120" cy="39" r="18" {...common} />
        </>
      )
    case 2:
      return (
        <>
          <rect x="96" y="48" width="48" height="25" rx="10" {...common} />
          <rect x="89" y="29" width="62" height="28" rx="13" {...common} />
        </>
      )
    case 3:
      return <path d="M92 70 L98 35 Q120 19 142 35 L148 70 Z" {...common} />
    case 4:
      return (
        <>
          <rect x="101" y="53" width="38" height="21" rx="2" {...common} />
          <path d="M90 53 L97 27 L143 27 L150 53 Z" {...common} />
        </>
      )
    default:
      return (
        <>
          <rect x="102" y="54" width="36" height="19" {...common} />
          <path d="M93 54 V34 Q93 26 101 26 H139 Q147 26 147 34 V54 Z" {...common} />
          <path d="M101 31 H139" stroke="#fff" strokeOpacity="0.35" />
        </>
      )
  }
}

function Decoration({ design }: { design: BottleDesign }) {
  switch (design.decoration) {
    case 0:
      return <path d="M62 113 H178 M62 221 H178" stroke={design.metal} strokeWidth="3" opacity="0.8" />
    case 1:
      return <circle cx="120" cy="157" r="38" fill="none" stroke={design.metal} strokeWidth="2" opacity="0.75" />
    case 2:
      return <path d="M70 87 L170 235 M170 87 L70 235" stroke="#fff" strokeWidth="2" opacity="0.14" />
    case 3:
      return <path d="M120 82 V237 M76 160 H164" stroke={design.metal} strokeWidth="2" opacity="0.45" />
    case 4:
      return (
        <>
          <path d="M73 229 Q120 198 167 229" fill="none" stroke={design.metal} strokeWidth="3" opacity="0.7" />
          <circle cx="120" cy="222" r="5" fill={design.metal} />
        </>
      )
    case 5:
      return <path d="M58 110 Q120 82 182 110" fill="none" stroke="#fff" strokeWidth="5" opacity="0.18" />
    default:
      return <path d="M82 86 L120 105 L158 86" fill="none" stroke={design.metal} strokeWidth="3" opacity="0.65" />
  }
}

function Label({
  design,
  fragrance,
}: {
  design: BottleDesign
  fragrance: Fragrance
}) {
  const lines = splitName(fragrance.name)
  const labelShape = design.decoration % 4
  const manufacturer = fragrance.manufacturer.length > 24
    ? `${fragrance.manufacturer.slice(0, 22)}…`
    : fragrance.manufacturer

  return (
    <g>
      {labelShape === 0 && <rect x="73" y="120" width="94" height="78" rx="2" fill={design.label} opacity="0.94" />}
      {labelShape === 1 && <ellipse cx="120" cy="159" rx="50" ry="43" fill={design.label} opacity="0.94" />}
      {labelShape === 2 && <path d="M76 128 L90 115 H150 L164 128 V194 H76 Z" fill={design.label} opacity="0.94" />}
      {labelShape === 3 && (
        <>
          <rect x="70" y="123" width="100" height="72" rx="12" fill={design.label} opacity="0.9" />
          <rect x="76" y="129" width="88" height="60" rx="9" fill="none" stroke={design.metal} strokeWidth="1.5" />
        </>
      )}
      <text
        x="120"
        y={lines.length === 1 ? 155 : 148}
        textAnchor="middle"
        fill={design.labelInk}
        className="generated-bottle__name"
      >
        {lines.map((line, index) => (
          <tspan x="120" dy={index === 0 ? 0 : 14} key={line}>{line}</tspan>
        ))}
      </text>
      <text
        x="120"
        y={lines.length === 1 ? 175 : 181}
        textAnchor="middle"
        fill={design.labelInk}
        className="generated-bottle__house"
      >
        {manufacturer}
      </text>
      <circle cx="120" cy="132" r="3" fill={design.metal} />
    </g>
  )
}

export function GeneratedBottleArtwork({
  fragrance,
  size = 'card',
  caption = true,
  scene = true,
}: GeneratedBottleArtworkProps) {
  const design = createDesign(fragrance)
  const reactId = useId().replace(/:/g, '')
  const bodyPath = bottleShapes[design.shape]
  const name = `${fragrance.name} by ${fragrance.manufacturer}`

  return (
    <figure className={`fragrance-artwork fragrance-artwork--${size}`}>
      <svg viewBox="0 0 240 300" role="img" aria-label={`Generated illustration inspired by ${name}`}>
        <defs>
          <linearGradient id={`glass-${reactId}`} x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stopColor="#fff" stopOpacity="0.82" />
            <stop offset="0.34" stopColor={design.glass} stopOpacity="0.76" />
            <stop offset="1" stopColor={design.glassEdge} stopOpacity="0.94" />
          </linearGradient>
          <linearGradient id={`liquid-${reactId}`} x1="0" x2="0" y1="0" y2="1">
            <stop offset="0" stopColor={design.liquid} stopOpacity="0.46" />
            <stop offset="1" stopColor={design.liquid} stopOpacity="0.88" />
          </linearGradient>
          <clipPath id={`body-${reactId}`}>
            <path d={bodyPath} />
          </clipPath>
          <filter id={`shadow-${reactId}`} x="-30%" y="-30%" width="160%" height="180%">
            <feGaussianBlur stdDeviation="7" />
          </filter>
        </defs>
        {scene && (
          <>
            <rect width="240" height="300" rx="10" fill={design.background} />
            <circle cx="38" cy="54" r="46" fill="#fff" opacity="0.2" />
          </>
        )}
        <ellipse
          cx="120"
          cy="264"
          rx="73"
          ry="12"
          fill="#1d2925"
          opacity="0.18"
          filter={`url(#shadow-${reactId})`}
        />
        <rect x="106" y="61" width="28" height="27" rx="3" fill={design.metal} stroke="#202522" strokeOpacity="0.25" />
        <Cap design={design} />
        <path
          d={bodyPath}
          fill={`url(#glass-${reactId})`}
          stroke={design.glassEdge}
          strokeWidth="2"
        />
        <g clipPath={`url(#body-${reactId})`}>
          <rect x="36" y="181" width="168" height="85" fill={`url(#liquid-${reactId})`} />
          <path d="M66 74 H84 L72 244 H56 Z" fill="#fff" opacity="0.2" />
          <path d="M172 75 H184 L193 232 L179 246 Z" fill="#101614" opacity="0.08" />
          <Decoration design={design} />
          <Label design={design} fragrance={fragrance} />
        </g>
        <path d="M77 249 Q120 257 163 249" fill="none" stroke="#fff" strokeOpacity="0.38" strokeWidth="2" />
      </svg>
      {caption && <figcaption>Generated fragrance interpretation</figcaption>}
    </figure>
  )
}
