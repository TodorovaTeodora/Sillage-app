import { useState, type FormEvent } from 'react'
import { requestPasswordReset, resetPassword } from '../api'
import { useAuth } from '../auth-context'
import { AuthControls } from './AuthControls'

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d).{5,}$/
const PASSWORD_MESSAGE =
  'Password must be at least 5 characters and include an uppercase letter and a number.'

type ResetStage = 'request' | 'reset' | 'success'

export function ForgotPasswordPage() {
  const { openAuth } = useAuth()
  const [stage, setStage] = useState<ResetStage>('request')
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function sendCode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const result = await requestPasswordReset(email)
      setMessage(result)
      setStage('reset')
    } catch (requestError) {
      setError(
        requestError instanceof Error ? requestError.message : 'Unable to send a reset code',
      )
    } finally {
      setSubmitting(false)
    }
  }

  async function changePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!PASSWORD_PATTERN.test(newPassword)) {
      setError(PASSWORD_MESSAGE)
      return
    }
    setSubmitting(true)
    setError('')
    try {
      await resetPassword(email, code, newPassword)
      setStage('success')
      setMessage('Your password has been reset. You can now log in.')
      setCode('')
      setNewPassword('')
    } catch (requestError) {
      setError(
        requestError instanceof Error ? requestError.message : 'Unable to reset your password',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="app-shell forgot-password-page">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Sillage home">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <AuthControls />
      </header>

      <main className="forgot-password-page__main">
        <section className="forgot-password-card">
          <span className="section-kicker">Account recovery</span>
          <h1>Forgot Password</h1>

          {stage === 'request' && (
            <>
              <p>Enter your account email and we will send you a six-digit reset code.</p>
              <form onSubmit={sendCode}>
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
                {error && <p className="forgot-password-card__error" role="alert">{error}</p>}
                <button type="submit" disabled={submitting}>
                  {submitting ? 'Sending...' : 'Send reset code'}
                </button>
              </form>
            </>
          )}

          {stage === 'reset' && (
            <>
              <p>{message}</p>
              <form onSubmit={changePassword}>
                <label>
                  <span>Email</span>
                  <input type="email" value={email} disabled />
                </label>
                <label>
                  <span>Six-digit code</span>
                  <input
                    type="text"
                    value={code}
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    required
                    minLength={6}
                    maxLength={6}
                    pattern="\d{6}"
                    onChange={(event) => setCode(event.target.value.replace(/\D/g, ''))}
                  />
                </label>
                <label>
                  <span>New password</span>
                  <input
                    type="password"
                    value={newPassword}
                    autoComplete="new-password"
                    required
                    maxLength={72}
                    onChange={(event) => setNewPassword(event.target.value)}
                  />
                </label>
                <p className="forgot-password-card__help">{PASSWORD_MESSAGE}</p>
                {error && <p className="forgot-password-card__error" role="alert">{error}</p>}
                <button type="submit" disabled={submitting}>
                  {submitting ? 'Resetting...' : 'Reset password'}
                </button>
                <button
                  type="button"
                  className="forgot-password-card__secondary"
                  disabled={submitting}
                  onClick={() => {
                    setStage('request')
                    setMessage('')
                    setError('')
                  }}
                >
                  Send another code
                </button>
              </form>
            </>
          )}

          {stage === 'success' && (
            <div className="forgot-password-card__success">
              <p>{message}</p>
              <button type="button" onClick={() => openAuth('login')}>Log in</button>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
