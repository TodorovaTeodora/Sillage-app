import { useState, type CSSProperties } from 'react'

type Metric = 'longevity' | 'sillage'

type Ratings = {
  longevity: number
  sillage: number
}

const STORAGE_KEY = 'sillage-performance-ratings'

const scales: Record<Metric, string[]> = {
  longevity: ['No vote', 'Very weak', 'Weak', 'Moderate', 'Long lasting', 'Eternal'],
  sillage: ['No vote', 'Intimate', 'Moderate', 'Strong', 'Enormous'],
}

function readRatings(fragranceId: string): Ratings {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (!stored) return { longevity: 0, sillage: 0 }
    const preferences = JSON.parse(stored) as Record<string, Partial<Ratings>>
    const ratings = preferences[fragranceId] ?? {}
    return {
      longevity: Math.min(
        scales.longevity.length - 1,
        Math.max(0, Number(ratings.longevity) || 0),
      ),
      sillage: Math.min(
        scales.sillage.length - 1,
        Math.max(0, Number(ratings.sillage) || 0),
      ),
    }
  } catch (error) {
    console.error('Unable to read performance ratings', error)
    return { longevity: 0, sillage: 0 }
  }
}

function saveRatings(fragranceId: string, ratings: Ratings) {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    const preferences = stored
      ? JSON.parse(stored) as Record<string, Ratings>
      : {}
    preferences[fragranceId] = ratings
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
  } catch (error) {
    console.error('Unable to save performance ratings', error)
  }
}

function PerformanceIcon({ metric }: { metric: Metric }) {
  if (metric === 'longevity') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <path d="M24 7a17 17 0 1 0 17 17" />
        <path d="M24 3v6M13 6l3 5M5 15l6 3M4 27h6M10 39l5-4M24 24V12M24 24l9 5" />
        <path d="M31 5l4 4M38 10l3 5M42 18l1 5" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <path d="M18 34a14 14 0 1 1 12 0" />
      <path d="M14 39a20 20 0 1 1 20 0" opacity="0.35" />
      <path d="M22 35h4M24 35V22" />
      <circle cx="24" cy="19" r="3" />
    </svg>
  )
}

export function PerformanceRatings({ fragranceId }: { fragranceId: string }) {
  const [ratings, setRatings] = useState<Ratings>(
    () => readRatings(fragranceId),
  )

  function update(metric: Metric, value: number) {
    setRatings((current) => {
      const next = { ...current, [metric]: value }
      saveRatings(fragranceId, next)
      return next
    })
  }

  return (
    <section className="performance-ratings" aria-label="Fragrance performance">
      {(Object.keys(scales) as Metric[]).map((metric) => {
        const labels = scales[metric]
        const value = ratings[metric]
        const progress = `${(value / (labels.length - 1)) * 100}%`
        return (
          <div className="performance-rating" key={metric}>
            <header>
              <span className="performance-rating__icon">
                <PerformanceIcon metric={metric} />
              </span>
              <h2>{metric}</h2>
            </header>
            <strong>{labels[value]}</strong>
            <input
              type="range"
              min="0"
              max={labels.length - 1}
              step="1"
              value={value}
              aria-label={`${metric} rating`}
              aria-valuetext={labels[value]}
              onChange={(event) => update(metric, Number(event.target.value))}
              style={{ '--rating-progress': progress } as CSSProperties}
            />
            <div
              className="performance-rating__steps"
              style={{ gridTemplateColumns: `repeat(${labels.length}, minmax(0, 1fr))` }}
              aria-hidden="true"
            >
              {labels.map((label, index) => (
                <span
                  className={index === value ? 'performance-rating__step performance-rating__step--active' : 'performance-rating__step'}
                  key={label}
                >
                  <i />
                  <small>{label}</small>
                </span>
              ))}
            </div>
          </div>
        )
      })}
    </section>
  )
}
