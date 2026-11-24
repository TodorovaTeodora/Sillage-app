import { useEffect, useState, type FormEvent } from 'react'
import { createPortal } from 'react-dom'
import {
  loginUser,
  logoutUser,
  registerUser,
} from '../api'
import { useAuth, type AuthMode } from '../auth-context'
import { CloseIcon } from './Icons'

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d).{5,}$/
const PASSWORD_MESSAGE =
  'Password must be at least 5 characters and include an uppercase letter and a number.'

export function AuthControls() {
  const { user, prompt, setUser, openAuth, closeAuth } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const mode = prompt?.mode ?? 'login'
  const open = prompt !== null

  useEffect(() => {
    if (!open) return
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') closeAuth()
    }
    window.addEventListener('keydown', closeOnEscape)
    document.body.classList.add('auth-modal-open')
    return () => {
      window.removeEventListener('keydown', closeOnEscape)
      document.body.classList.remove('auth-modal-open')
    }
  }, [closeAuth, open])

  function showAuth(nextMode: AuthMode) {
    setError('')
    setPassword('')
    openAuth(nextMode)
  }

  function switchMode(nextMode: AuthMode) {
    setError('')
    setPassword('')
    openAuth(nextMode, prompt?.message)
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (mode === 'register' && !PASSWORD_PATTERN.test(password)) {
      setError(PASSWORD_MESSAGE)
      return
    }

    setSubmitting(true)
    setError('')
    try {
      const authenticatedUser = mode === 'register'
        ? await registerUser(email, password)
        : await loginUser(email, password)
      setUser(authenticatedUser)
      closeAuth()
      setEmail('')
      setPassword('')
      window.location.assign('/account')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Authentication failed')
    } finally {
      setSubmitting(false)
    }
  }

  async function logout() {
    setError('')
    try {
      await logoutUser()
      setUser(null)
    } catch (requestError) {
      console.error('Unable to log out', requestError)
    }
  }

  return (
    <>
      <div className="auth-controls">
        {user ? (
          <>
            <a className="auth-controls__account" href="/account" title={user.email}>
              <span>My Account</span>
              {user.username && <small>{user.username}</small>}
            </a>
            <button type="button" className="auth-trigger" onClick={logout}>Log out</button>
          </>
        ) : (
          <>
            <button type="button" className="auth-trigger" onClick={() => showAuth('login')}>
              Log in
            </button>
            <button
              type="button"
              className="auth-trigger auth-trigger--primary"
              onClick={() => showAuth('register')}
            >
              Register
            </button>
          </>
        )}
      </div>

      {open && createPortal(
        <div
          className="auth-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeAuth()
          }}
        >
          <section
            className="auth-modal__panel"
            role="dialog"
            aria-modal="true"
            aria-labelledby="auth-modal-title"
          >
            <button
              type="button"
              className="auth-modal__close"
              aria-label="Close"
              onClick={closeAuth}
            >
              <CloseIcon />
            </button>
            <span className="section-kicker">Your Sillage account</span>
            <h2 id="auth-modal-title">
              {mode === 'register' ? 'Create an account' : 'Welcome back'}
            </h2>
            {prompt.message && (
              <p className="auth-modal__notice">{prompt.message}</p>
            )}
            <div className="auth-modal__tabs" role="tablist" aria-label="Authentication">
              <button
                type="button"
                role="tab"
                aria-selected={mode === 'login'}
                onClick={() => switchMode('login')}
              >
                Log in
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={mode === 'register'}
                onClick={() => switchMode('register')}
              >
                Register
              </button>
            </div>
            <form onSubmit={submit}>
              <label>
                <span>Email</span>
                <input
                  type="email"
                  value={email}
                  autoComplete="email"
                  required
                  maxLength={254}
                  onChange={(event) => setEmail(event.target.value)}
                />
              </label>
              <label>
                <span>Password</span>
                <input
                  type="password"
                  value={password}
                  autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
                  required
                  maxLength={72}
                  onChange={(event) => setPassword(event.target.value)}
                />
              </label>
              {mode === 'register' && (
                <p className="auth-modal__help">{PASSWORD_MESSAGE}</p>
              )}
              {error && <p className="auth-modal__error" role="alert">{error}</p>}
              <button className="auth-modal__submit" type="submit" disabled={submitting}>
                {submitting
                  ? 'Please wait...'
                  : mode === 'register' ? 'Create account' : 'Log in'}
              </button>
            </form>
          </section>
        </div>,
        document.body,
      )}
    </>
  )
}
