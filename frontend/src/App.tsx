import { useEffect, useMemo, useState, type CSSProperties } from 'react'
import {
  getFacets,
  getFragrance,
  getFragranceImages,
  getFragranceMedia,
  getFragrances,
} from './api'
import { CloseIcon, HeartIcon, SearchIcon } from './components/Icons'
import { AuthControls } from './components/AuthControls'
import { GeneratedBottleArtwork } from './components/GeneratedBottleArtwork'
import { MediaAttribution } from './components/MediaAttribution'
import { NotePyramid } from './components/NotePyramid'
import { PerformanceRatings } from './components/PerformanceRatings'
import { WhenToWear } from './components/WhenToWear'
import { AccountPage } from './components/AccountPage'
import { CommentsSection } from './components/CommentsSection'
import type {
  Fragrance,
  FragranceFacets,
  FragranceFilters,
  FragranceMedia,
  MediaAsset,
} from './types'
import { useFavorites } from './useFavorites'
import { AuthProvider } from './AuthContext'
import { useAuth } from './auth-context'
import './App.css'

const emptyFilters: FragranceFilters = {
  query: '',
  manufacturers: [],
}

const tones = ['#b26845', '#506b5f', '#8b6a7c', '#9c793a', '#536c86', '#765b43']

function toneFor(id: string) {
  const score = [...id].reduce((total, character) => total + character.charCodeAt(0), 0)
  return tones[score % tones.length]
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

function AppRoutes() {
  const detailMatch = window.location.pathname.match(/^\/fragrances\/([^/]+)\/?$/)
  if (detailMatch) {
    return <FragrancePage fragranceId={decodeURIComponent(detailMatch[1])} />
  }
  if (/^\/collection\/?$/.test(window.location.pathname)) {
    return <CollectionPage />
  }
  if (/^\/account\/?$/.test(window.location.pathname)) {
    return <AccountPage />
  }
  return <CatalogApp />
}

function CatalogApp() {
  const [filters, setFilters] = useState<FragranceFilters>(emptyFilters)
  const [fragrances, setFragrances] = useState<Fragrance[]>([])
  const [facets, setFacets] = useState<FragranceFacets | null>(null)
  const [selected, setSelected] = useState<Fragrance | null>(null)
  const [favoritesOnly, setFavoritesOnly] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [searchFocused, setSearchFocused] = useState(false)
  const [cardImages, setCardImages] = useState<Record<string, MediaAsset>>({})
  const { favorites, toggleFavorite } = useFavorites()

  useEffect(() => {
    const controller = new AbortController()
    getFacets(controller.signal)
      .then(setFacets)
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message)
        }
      })
    return () => controller.abort()
  }, [reloadKey])

  useEffect(() => {
    const controller = new AbortController()
    if (filters.query.trim().length === 1) {
      return () => controller.abort()
    }
    const timeout = window.setTimeout(() => {
      setLoading(true)
      setError('')
      getFragrances(filters, controller.signal)
        .then(setFragrances)
        .catch((requestError: Error) => {
          if (requestError.name !== 'AbortError') {
            setError(requestError.message)
          }
        })
        .finally(() => setLoading(false))
    }, 400)

    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [filters, reloadKey])

  useEffect(() => {
    if (fragrances.length === 0) return
    const controller = new AbortController()
    const timeout = window.setTimeout(() => {
      getFragranceImages(fragrances, controller.signal)
        .then((result) => {
          setCardImages(result.images)
        })
        .catch((requestError: Error) => {
          if (requestError.name !== 'AbortError') {
            console.error('Unable to load licensed fragrance thumbnails', requestError)
          }
        })
    }, 700)

    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [fragrances])

  useEffect(() => {
    if (!selected) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setSelected(null)
    }
    window.addEventListener('keydown', onKeyDown)
    document.body.classList.add('drawer-open')
    return () => {
      window.removeEventListener('keydown', onKeyDown)
      document.body.classList.remove('drawer-open')
    }
  }, [selected])

  const visibleFragrances = useMemo(
    () =>
      favoritesOnly
        ? fragrances.filter((fragrance) => favorites.has(fragrance.id))
        : fragrances,
    [favorites, favoritesOnly, fragrances],
  )

  const activeFilterCount =
    (filters.query ? 1 : 0) + filters.manufacturers.length
  const searchSuggestions = filters.query.trim().length >= 2 ? fragrances.slice(0, 6) : []

  function updateQuery(value: string) {
    setFilters((current) => ({ ...current, query: value }))
  }

  function toggleManufacturer(manufacturer: string) {
    setFilters((current) => ({
      ...current,
      manufacturers: current.manufacturers.includes(manufacturer)
        ? current.manufacturers.filter((item) => item !== manufacturer)
        : [...current.manufacturers, manufacturer],
    }))
  }

  function clearFilters() {
    setFilters(emptyFilters)
    setFavoritesOnly(false)
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Sillage home">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <nav aria-label="Primary navigation">
          <a href="/#discover">Discover</a>
          <a className="nav-favorite" href="/collection">
            <HeartIcon filled={favorites.size > 0} />
            My collection
            {favorites.size > 0 && <span>{favorites.size}</span>}
          </a>
          <AuthControls />
        </nav>
      </header>

      <main>
        <section className="hero-section">
          <div className="hero-section__eyebrow">The fragrance archive</div>
          <h1>Sillage helps you find and store your favorite scents</h1>
          <p>
            Search iconic fragrances by name or perfume house, then explore
            every layer of their composition.
          </p>
          <div
            className="hero-search-wrap"
            onBlur={(event) => {
              if (!event.currentTarget.contains(event.relatedTarget)) {
                setSearchFocused(false)
              }
            }}
          >
            <div className="hero-search">
              <SearchIcon />
              <label className="sr-only" htmlFor="fragrance-search">
                Search by fragrance or perfume house
              </label>
              <input
                id="fragrance-search"
                type="search"
                placeholder="Search a fragrance or perfume house..."
                value={filters.query}
                aria-autocomplete="list"
                aria-controls="fragrance-suggestions"
                aria-expanded={searchFocused && Boolean(filters.query.trim())}
                onChange={(event) => updateQuery(event.target.value)}
                onFocus={() => setSearchFocused(true)}
              />
              <button
                className="hero-search__cancel"
                type="button"
                disabled={!filters.query}
                onClick={() => {
                  updateQuery('')
                  setSearchFocused(false)
                }}
              >
                Cancel
              </button>
            </div>
            {searchFocused && filters.query.trim() && (
              <div className="search-suggestions" id="fragrance-suggestions" role="listbox">
                {loading && <p>Searching the archive...</p>}
                {!loading && filters.query.trim().length === 1 && (
                  <p>Type at least 2 characters to search.</p>
                )}
                {!loading && searchSuggestions.map((fragrance) => (
                  <a
                    href={`/fragrances/${encodeURIComponent(fragrance.id)}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    role="option"
                    key={fragrance.id}
                  >
                    <span className="search-suggestions__monogram">
                      {fragrance.name.charAt(0)}
                    </span>
                    <span>
                      <strong>{fragrance.name}</strong>
                      <small>
                        {fragrance.manufacturer}
                        {fragrance.year !== null && ` / ${fragrance.year}`}
                      </small>
                    </span>
                    <i>Open</i>
                  </a>
                ))}
                {!loading && filters.query.trim().length >= 2 && searchSuggestions.length === 0 && (
                  <p>No fragrance or perfume house matches that search.</p>
                )}
              </div>
            )}
          </div>
          <div className="hero-stats" aria-label="Catalog summary">
            <span><strong>{facets?.totalCount ?? '--'}</strong> curated scents</span>
            <span><strong>{facets?.manufacturerCount ?? '--'}</strong> perfume houses</span>
            <span><strong>{facets?.noteCount ?? '--'}</strong> distinct notes</span>
          </div>
        </section>

        <section className="catalog" id="discover">
          <div className="catalog-heading">
            <div>
              <span className="section-kicker">Browse the collection</span>
              <h2>Popular scents</h2>
            </div>
            <p>{visibleFragrances.length} {visibleFragrances.length === 1 ? 'result' : 'results'}</p>
          </div>

          <div className="catalog-layout">
            <aside className="filters" aria-label="Fragrance filters">
              <div className="filters__heading">
                <h3>Refine</h3>
                {(activeFilterCount > 0 || favoritesOnly) && (
                  <button type="button" onClick={clearFilters}>Clear all</button>
                )}
              </div>

              <fieldset className="brand-filter">
                <legend>Brand</legend>
                <details>
                  <summary>
                    {filters.manufacturers.length > 0
                      ? `${filters.manufacturers.length} selected`
                      : 'Select brands'}
                  </summary>
                  <div className="brand-filter__options">
                    {(facets?.manufacturers ?? []).map((manufacturer) => (
                      <label key={manufacturer}>
                        <input
                          type="checkbox"
                          checked={filters.manufacturers.includes(manufacturer)}
                          onChange={() => toggleManufacturer(manufacturer)}
                        />
                        <span>{manufacturer}</span>
                      </label>
                    ))}
                  </div>
                </details>
              </fieldset>

              <label className="favorite-filter">
                <input
                  type="checkbox"
                  checked={favoritesOnly}
                  onChange={(event) => setFavoritesOnly(event.target.checked)}
                />
                <span><HeartIcon filled={favoritesOnly} /> My collection only</span>
              </label>
            </aside>

            <div className="results">
              {error && (
                <div className="status-card status-card--error">
                  <h3>The archive could not be reached</h3>
                  <p>{error}</p>
                  <button type="button" onClick={() => setReloadKey((key) => key + 1)}>
                    Try again
                  </button>
                </div>
              )}

              {!error && loading && (
                <div className="fragrance-grid" aria-label="Loading fragrances">
                  {[0, 1, 2, 3, 4, 5].map((item) => (
                    <div className="fragrance-card fragrance-card--loading" key={item} />
                  ))}
                </div>
              )}

              {!error && !loading && visibleFragrances.length === 0 && (
                <div className="status-card">
                  <span className="status-card__mark">S</span>
                  <h3>No matching fragrances</h3>
                  <p>Try another brand or remove one of your filters.</p>
                  <button type="button" onClick={clearFilters}>Reset filters</button>
                </div>
              )}

              {!error && !loading && visibleFragrances.length > 0 && (
                <div className="fragrance-grid">
                  {visibleFragrances.map((fragrance) => {
                    const isFavorite = favorites.has(fragrance.id)
                    return (
                      <article
                        className="fragrance-card"
                        key={fragrance.id}
                        style={{ '--tone': toneFor(fragrance.id) } as CSSProperties}
                      >
                        <button
                          className={`favorite-button${isFavorite ? ' favorite-button--active' : ''}`}
                          type="button"
                          aria-label={`${isFavorite ? 'Remove' : 'Add'} ${fragrance.name} ${isFavorite ? 'from' : 'to'} my collection`}
                          onClick={() => toggleFavorite(fragrance.id)}
                        >
                          <HeartIcon filled={isFavorite} />
                        </button>
                        <button
                          className="fragrance-card__main"
                          type="button"
                          onClick={() => setSelected(fragrance)}
                        >
                          {cardImages[fragrance.id]
                            ? (
                              <figure className="fragrance-card__photo">
                                <img
                                  src={cardImages[fragrance.id].imageUrl}
                                  alt={cardImages[fragrance.id].alt}
                                  loading="lazy"
                                />
                                <MediaAttribution asset={cardImages[fragrance.id]} />
                              </figure>
                            )
                            : <FragranceFallbackImage fragrance={fragrance} />}
                          <span className="fragrance-card__house">{fragrance.manufacturer}</span>
                          <h3>{fragrance.name}</h3>
                          <span className="fragrance-card__meta">
                            {fragrance.year !== null && `${fragrance.year} / `}
                            {fragrance.family}
                          </span>
                          <span className="fragrance-card__notes">
                            {fragrance.pyramid.top.slice(0, 2).join(' / ')}
                          </span>
                        </button>
                      </article>
                    )
                  })}
                </div>
              )}
            </div>
          </div>
        </section>
      </main>

      <footer>
        <a className="brand brand--footer" href="#">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <p>Built around a replaceable fragrance data provider.</p>
        <a
          className="data-credit"
          href="https://10ml.com/perfume-api"
          target="_blank"
          rel="noopener noreferrer"
        >
          Data: 10ML Fragrance Index
        </a>
      </footer>

      {selected && (
        <FragranceDrawer
          fragrance={selected}
          favorite={favorites.has(selected.id)}
          onClose={() => setSelected(null)}
          onToggleFavorite={() => toggleFavorite(selected.id)}
        />
      )}
    </div>
  )
}

type CollectionLoadState = {
  key: string
  fragrances: Fragrance[]
  failedCount: number
}

function CollectionPage() {
  const { user, ready: authReady, openAuth } = useAuth()
  const { favorites, loading: favoritesLoading, toggleFavorite } = useFavorites()
  const favoriteIds = useMemo(() => [...favorites].sort(), [favorites])
  const favoriteKey = favoriteIds.join('|')
  const [loadState, setLoadState] = useState<CollectionLoadState>({
    key: '',
    fragrances: [],
    failedCount: 0,
  })

  useEffect(() => {
    document.title = 'My collection | Sillage'
  }, [])

  useEffect(() => {
    if (!favoriteKey) return
    const controller = new AbortController()
    Promise.allSettled(
      favoriteIds.map((id) => getFragrance(id, controller.signal)),
    ).then((results) => {
      if (controller.signal.aborted) return
      const fragrances = results.flatMap((result) =>
        result.status === 'fulfilled' ? [result.value] : [],
      )
      setLoadState({
        key: favoriteKey,
        fragrances,
        failedCount: results.length - fragrances.length,
      })
    })
    return () => controller.abort()
  }, [favoriteIds, favoriteKey])

  const loading =
    favoritesLoading || (favoriteKey !== '' && loadState.key !== favoriteKey)
  const fragrances = loadState.fragrances.filter((fragrance) =>
    favorites.has(fragrance.id),
  )

  return (
    <div className="app-shell collection-page">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Sillage home">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <nav aria-label="Primary navigation">
          <a href="/#discover">Discover</a>
          <a className="nav-favorite nav-favorite--active" href="/collection" aria-current="page">
            <HeartIcon filled />
            My collection
            {favorites.size > 0 && <span>{favorites.size}</span>}
          </a>
          <AuthControls />
        </nav>
      </header>

      <main className="collection-page__main">
        <section className="collection-page__heading">
          <span className="section-kicker">Your fragrance wardrobe</span>
          <h1>My collection</h1>
          <p>
            Every fragrance you save is kept here on your personal shelf.
          </p>
        </section>

        {authReady && !user && (
          <section className="collection-empty">
            <span className="status-card__mark">S</span>
            <h2>Log in to view your collection</h2>
            <p>Your saved fragrances are securely linked to your Sillage account.</p>
            <button type="button" onClick={() => openAuth('login')}>
              Log in
            </button>
          </section>
        )}

        {user && !favoritesLoading && favoriteKey === '' && (
          <section className="collection-empty">
            <span className="status-card__mark">S</span>
            <h2>Your shelf is waiting</h2>
            <p>Save fragrances from the archive to build your collection.</p>
            <a href="/#discover">Discover fragrances</a>
          </section>
        )}

        {user && loading && (
          <div className="collection-loading" aria-label="Loading collection">
            {[0, 1, 2].map((item) => <span key={item} />)}
          </div>
        )}

        {user && !loading && favoriteKey !== '' && (
          <>
            {loadState.failedCount > 0 && (
              <p className="collection-warning">
                {loadState.failedCount} saved fragrance could not be loaded.
              </p>
            )}
            <section className="collection-shelf-grid" aria-label="Saved fragrances">
              {fragrances.map((fragrance) => (
                <article
                  className="collection-bottle"
                  style={{ '--tone': toneFor(fragrance.id) } as CSSProperties}
                  key={fragrance.id}
                >
                  <a
                    className="collection-bottle__link"
                    href={`/fragrances/${encodeURIComponent(fragrance.id)}`}
                  >
                    <GeneratedBottleArtwork
                      fragrance={fragrance}
                      size="shelf"
                      caption={false}
                      scene={false}
                    />
                    <span>{fragrance.manufacturer}</span>
                    <h2>{fragrance.name}</h2>
                  </a>
                  <button
                    type="button"
                    onClick={() => toggleFavorite(fragrance.id)}
                    aria-label={`Remove ${fragrance.name} from my collection`}
                  >
                    <HeartIcon filled />
                    Remove
                  </button>
                </article>
              ))}
            </section>
          </>
        )}
      </main>
    </div>
  )
}

function FragranceFallbackImage({
  fragrance,
  large = false,
}: {
  fragrance: Fragrance
  large?: boolean
}) {
  return <GeneratedBottleArtwork fragrance={fragrance} size={large ? 'large' : 'card'} />
}

type DrawerProps = {
  fragrance: Fragrance
  favorite: boolean
  onClose: () => void
  onToggleFavorite: () => void
}

function FragranceDrawer({
  fragrance,
  favorite,
  onClose,
  onToggleFavorite,
}: DrawerProps) {
  const [media, setMedia] = useState<FragranceMedia | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    getFragranceMedia(fragrance, controller.signal)
      .then(setMedia)
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          console.error('Unable to load licensed fragrance media', requestError)
        }
      })
    return () => controller.abort()
  }, [fragrance])

  return (
    <div className="drawer-backdrop" onMouseDown={onClose}>
      <section
        className="drawer"
        role="dialog"
        aria-modal="true"
        aria-labelledby="fragrance-title"
        onMouseDown={(event) => event.stopPropagation()}
        style={{ '--tone': toneFor(fragrance.id) } as CSSProperties}
      >
        <button className="drawer__close" type="button" onClick={onClose} aria-label="Close details">
          <CloseIcon />
        </button>
        <div className="drawer__visual">
          <GeneratedBottleArtwork fragrance={fragrance} size="display" caption={false} />
          <span>{fragrance.family}</span>
        </div>
        <div className="drawer__content">
          <span className="drawer__house">{fragrance.manufacturer}</span>
          <h2 id="fragrance-title">{fragrance.name}</h2>
          <div className="drawer__meta">
            {fragrance.year !== null && (
              <span><small>Released</small>{fragrance.year}</span>
            )}
            <span><small>Concentration</small>{fragrance.concentration}</span>
          </div>
          <p className="drawer__description">{fragrance.description}</p>
          <WhenToWear fragranceId={fragrance.id} />
          <PerformanceRatings fragranceId={fragrance.id} />
          <div className="drawer__pyramid">
            <span className="section-kicker">The composition</span>
            <h3>Note pyramid</h3>
            <NotePyramid
              pyramid={fragrance.pyramid}
              noteImages={media?.noteImages}
            />
          </div>
          <button
            className={`drawer__favorite${favorite ? ' drawer__favorite--active' : ''}`}
            type="button"
            onClick={onToggleFavorite}
          >
            <HeartIcon filled={favorite} />
            {favorite ? 'Saved to my collection' : 'Save to my collection'}
          </button>
        </div>
      </section>
    </div>
  )
}

function FragrancePage({ fragranceId }: { fragranceId: string }) {
  const [fragrance, setFragrance] = useState<Fragrance | null>(null)
  const [media, setMedia] = useState<FragranceMedia | null>(null)
  const [error, setError] = useState('')
  const { favorites, toggleFavorite } = useFavorites()

  useEffect(() => {
    const controller = new AbortController()
    getFragrance(fragranceId, controller.signal)
      .then((result) => {
        setFragrance(result)
        document.title = `${result.name} by ${result.manufacturer} | Sillage`
        getFragranceMedia(result, controller.signal)
          .then(setMedia)
          .catch((requestError: Error) => {
            if (requestError.name !== 'AbortError') {
              console.error('Unable to load licensed fragrance media', requestError)
            }
          })
      })
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message)
        }
      })
    return () => controller.abort()
  }, [fragranceId])

  return (
    <div className="app-shell detail-page">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Sillage home">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <div className="detail-page__actions">
          <a className="detail-page__back" href="/">Back to the archive</a>
          <AuthControls />
        </div>
      </header>

      {error && (
        <main className="detail-page__status">
          <div className="status-card status-card--error">
            <h1>Fragrance not found</h1>
            <p>{error}</p>
            <a href="/">Return to the fragrance library</a>
          </div>
        </main>
      )}

      {!error && !fragrance && (
        <main className="detail-page__status">
          <div className="fragrance-card fragrance-card--loading" />
        </main>
      )}

      {fragrance && (
        <>
          <main
            className="detail-page__main"
            style={{ '--tone': toneFor(fragrance.id) } as CSSProperties}
          >
            <section className="detail-page__visual">
            {media?.fragranceImage
              ? (
                <figure className="detail-page__photo">
                  <img
                    src={media.fragranceImage.imageUrl}
                    alt={media.fragranceImage.alt}
                  />
                  <MediaAttribution asset={media.fragranceImage} />
                </figure>
              )
              : <FragranceFallbackImage fragrance={fragrance} large />}
            <span>{fragrance.family}</span>
            </section>
            <article className="detail-page__content">
            <span className="drawer__house">{fragrance.manufacturer}</span>
            <h1>{fragrance.name}</h1>
            <div className="drawer__meta">
              {fragrance.year !== null && (
                <span><small>Released</small>{fragrance.year}</span>
              )}
              <span><small>Concentration</small>{fragrance.concentration}</span>
            </div>
            <p className="drawer__description">{fragrance.description}</p>
            <WhenToWear fragranceId={fragrance.id} />
            <PerformanceRatings fragranceId={fragrance.id} />
            <div className="drawer__pyramid">
              <span className="section-kicker">The composition</span>
              <h2>Note pyramid</h2>
              <NotePyramid
                pyramid={fragrance.pyramid}
                noteImages={media?.noteImages}
              />
            </div>
            <button
              className={`drawer__favorite${favorites.has(fragrance.id) ? ' drawer__favorite--active' : ''}`}
              type="button"
              onClick={() => toggleFavorite(fragrance.id)}
            >
              <HeartIcon filled={favorites.has(fragrance.id)} />
              {favorites.has(fragrance.id) ? 'Saved to my collection' : 'Save to my collection'}
            </button>
            <a
              className="detail-page__credit data-credit"
              href="https://10ml.com/perfume-api"
              target="_blank"
              rel="noopener noreferrer"
            >
              Data: 10ML Fragrance Index
            </a>
            </article>
          </main>
          <CommentsSection fragranceId={fragrance.id} />
        </>
      )}
    </div>
  )
}

export default App
