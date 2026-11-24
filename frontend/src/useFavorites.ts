import { useEffect, useState } from 'react'
import {
  addToCollection,
  ApiError,
  getCollection,
  removeFromCollection,
} from './api'
import { useAuth } from './auth-context'

const EMPTY_FAVORITES = new Set<string>()

type FavoritesState = {
  owner: string
  favorites: Set<string>
}

export function useFavorites() {
  const { user, ready, openAuth, setUser } = useAuth()
  const [state, setState] = useState<FavoritesState>({
    owner: '',
    favorites: EMPTY_FAVORITES,
  })

  useEffect(() => {
    if (!ready || !user) return

    const controller = new AbortController()
    getCollection(controller.signal)
      .then((ids) => setState({
        owner: user.email,
        favorites: new Set(ids),
      }))
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          window.alert(requestError.message || 'Unable to load My collection')
          setState({ owner: user.email, favorites: EMPTY_FAVORITES })
        }
      })
    return () => controller.abort()
  }, [ready, user])

  const favorites =
    user && state.owner === user.email ? state.favorites : EMPTY_FAVORITES
  const loading = !ready || Boolean(user && state.owner !== user.email)

  function requireLogin() {
    openAuth(
      'login',
      'Please log in or register before adding fragrances to My collection.',
    )
  }

  async function toggleFavorite(id: string) {
    if (!user) {
      requireLogin()
      return
    }

    const removing = favorites.has(id)
    try {
      if (removing) {
        await removeFromCollection(id)
      } else {
        await addToCollection(id)
      }
      setState((current) => {
        const next = new Set(
          current.owner === user.email ? current.favorites : EMPTY_FAVORITES,
        )
        if (removing) {
          next.delete(id)
        } else {
          next.add(id)
        }
        return { owner: user.email, favorites: next }
      })
    } catch (requestError) {
      if (requestError instanceof ApiError && requestError.status === 401) {
        setUser(null)
        requireLogin()
        return
      }
      window.alert(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to update My collection',
      )
    }
  }

  return { favorites, loading, toggleFavorite }
}
