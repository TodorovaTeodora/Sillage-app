import { createContext, useContext } from 'react'
import type { AuthUser } from './types'

export type AuthMode = 'login' | 'register'

export type AuthPrompt = {
  mode: AuthMode
  message?: string
}

export type AuthContextValue = {
  user: AuthUser | null
  ready: boolean
  prompt: AuthPrompt | null
  setUser: (user: AuthUser | null) => void
  openAuth: (mode?: AuthMode, message?: string) => void
  closeAuth: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
