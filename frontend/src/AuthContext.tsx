import {
  useCallback,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { getCurrentUser } from './api'
import type { AuthUser } from './types'
import {
  AuthContext,
  type AuthMode,
  type AuthPrompt,
} from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [ready, setReady] = useState(false)
  const [prompt, setPrompt] = useState<AuthPrompt | null>(null)
  const openAuth = useCallback(
    (mode: AuthMode = 'login', message?: string) => setPrompt({ mode, message }),
    [],
  )
  const closeAuth = useCallback(() => setPrompt(null), [])

  useEffect(() => {
    const controller = new AbortController()
    getCurrentUser(controller.signal)
      .then(setUser)
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          console.error('Unable to restore the login session', requestError)
        }
      })
      .finally(() => setReady(true))
    return () => controller.abort()
  }, [])

  return (
    <AuthContext.Provider
      value={{
        user,
        ready,
        prompt,
        setUser,
        openAuth,
        closeAuth,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
