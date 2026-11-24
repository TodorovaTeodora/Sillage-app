import { useState, type CSSProperties } from 'react'

type WearOption = 'winter' | 'spring' | 'summer' | 'fall' | 'day' | 'night'

type Option = {
  id: WearOption
  label: string
  color: string
}

const STORAGE_KEY = 'sillage-when-to-wear'

const options: Option[] = [
  { id: 'winter', label: 'Winter', color: '#58bdd9' },
  { id: 'spring', label: 'Spring', color: '#78cd55' },
  { id: 'summer', label: 'Summer', color: '#f37f7b' },
  { id: 'fall', label: 'Fall', color: '#f4ad52' },
  { id: 'day', label: 'Day', color: '#f39200' },
  { id: 'night', label: 'Night', color: '#72a9ca' },
]

function readSelections(fragranceId: string) {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (!stored) return new Set<WearOption>()
    const preferences = JSON.parse(stored) as Record<string, WearOption[]>
    return new Set(preferences[fragranceId] ?? [])
  } catch (error) {
    console.error('Unable to read when-to-wear preferences', error)
    return new Set<WearOption>()
  }
}

function saveSelections(fragranceId: string, selections: Set<WearOption>) {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    const preferences = stored
      ? JSON.parse(stored) as Record<string, WearOption[]>
      : {}
    preferences[fragranceId] = [...selections]
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
  } catch (error) {
    console.error('Unable to save when-to-wear preferences', error)
  }
}

function WearIcon({ option }: { option: WearOption }) {
  if (option === 'winter') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <path d="M24 5v38M8 14l32 20M8 34l32-20M19 9l5 5 5-5M19 39l5-5 5 5M8 20l7 2-2-7M40 28l-7-2 2 7M8 28l7-2-2 7M40 20l-7 2 2-7" />
      </svg>
    )
  }
  if (option === 'spring') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <path d="M39 7C22 10 12 19 11 34c8 2 17-1 22-8 5-7 6-14 6-19Z" />
        <path d="M9 41c5-11 13-19 25-27" />
      </svg>
    )
  }
  if (option === 'summer') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <path d="M7 25C8 13 15 7 24 7s16 6 17 18c-5-4-10-4-14 0-4-4-8-4-12 0-3-3-5-3-8 0Z" />
        <path d="M24 7v32c0 3 2 4 4 4 3 0 5-2 5-5" />
      </svg>
    )
  }
  if (option === 'fall') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <path d="M22 8c-8 3-13 9-12 17 6 1 11-2 14-7 2-4 1-7-2-10ZM35 17c-7 1-11 5-12 12 5 2 10 0 13-4 2-3 2-6-1-8ZM18 30c-6 0-10 3-12 9 4 3 9 2 12-1 3-2 3-5 0-8Z" />
        <path d="M11 41c8-8 15-15 25-24" />
      </svg>
    )
  }
  if (option === 'day') {
    return (
      <svg viewBox="0 0 48 48" aria-hidden="true">
        <circle cx="24" cy="24" r="10" />
        <path d="M24 3v7M24 38v7M3 24h7M38 24h7M9 9l5 5M34 34l5 5M39 9l-5 5M14 34l-5 5" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <path d="M34 35A17 17 0 0 1 20 8a17 17 0 1 0 14 27Z" />
      <path d="m35 8 1.5 3.5L40 13l-3.5 1.5L35 18l-1.5-3.5L30 13l3.5-1.5L35 8ZM41 22l1 2.2 2 1-2 1-1 2.3-1-2.3-2-1 2-1 1-2.2Z" />
    </svg>
  )
}

export function WhenToWear({ fragranceId }: { fragranceId: string }) {
  const [selected, setSelected] = useState<Set<WearOption>>(
    () => readSelections(fragranceId),
  )

  function toggle(option: WearOption) {
    setSelected((current) => {
      const next = new Set(current)
      if (next.has(option)) {
        next.delete(option)
      } else {
        next.add(option)
      }
      saveSelections(fragranceId, next)
      return next
    })
  }

  return (
    <section className="when-to-wear" aria-labelledby="when-to-wear-title">
      <header>
        <span className="when-to-wear__clock" aria-hidden="true" />
        <div>
          <h2 id="when-to-wear-title">When to wear</h2>
          <p>Select every season and time that suits this fragrance.</p>
        </div>
      </header>
      <div className="when-to-wear__options">
        {options.map((option) => {
          const isSelected = selected.has(option.id)
          return (
            <button
              className={isSelected ? 'when-to-wear__option when-to-wear__option--selected' : 'when-to-wear__option'}
              type="button"
              aria-pressed={isSelected}
              onClick={() => toggle(option.id)}
              style={{ '--wear-color': option.color } as CSSProperties}
              key={option.id}
            >
              <span className="when-to-wear__icon">
                <WearIcon option={option.id} />
              </span>
              <b>{option.label}</b>
              <span className="when-to-wear__track"><i /></span>
              <small>{isSelected ? '1 vote' : '0 votes'}</small>
            </button>
          )
        })}
      </div>
    </section>
  )
}
