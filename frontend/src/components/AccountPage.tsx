import { useEffect, useState, type FormEvent } from 'react'
import {
  ApiError,
  getAccountProfile,
  updateAccountPassword,
  updateAccountProfile,
} from '../api'
import { useAuth } from '../auth-context'
import type { AccountProfile } from '../types'
import { AuthControls } from './AuthControls'
import { HeartIcon } from './Icons'

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d).{5,}$/
const PASSWORD_MESSAGE =
  'Password must be at least 5 characters and include an uppercase letter and a number.'

const emptyProfile = {
  username: '',
  country: '',
  city: '',
}

export function AccountPage() {
  const { user, ready, setUser, openAuth } = useAuth()
  const [profile, setProfile] = useState(emptyProfile)
  const [loaded, setLoaded] = useState(false)
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileMessage, setProfileMessage] = useState('')
  const [profileError, setProfileError] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [savingPassword, setSavingPassword] = useState(false)
  const [passwordMessage, setPasswordMessage] = useState('')
  const [passwordError, setPasswordError] = useState('')

  useEffect(() => {
    if (!ready || !user) return
    const controller = new AbortController()
    getAccountProfile(controller.signal)
      .then((account: AccountProfile) => {
        setProfile({
          username: account.username ?? '',
          country: account.country ?? '',
          city: account.city ?? '',
        })
        setLoaded(true)
      })
      .catch((requestError: Error) => {
        if (requestError.name === 'AbortError') return
        if (requestError instanceof ApiError && requestError.status === 401) {
          setUser(null)
          openAuth('login', 'Please log in to manage your account.')
          return
        }
        setProfileError(requestError.message)
      })
    return () => controller.abort()
  }, [openAuth, ready, setUser, user])

  async function saveProfile(nextProfile = profile) {
    setSavingProfile(true)
    setProfileError('')
    setProfileMessage('')
    try {
      const updated = await updateAccountProfile(nextProfile)
      setProfile({
        username: updated.username ?? '',
        country: updated.country ?? '',
        city: updated.city ?? '',
      })
      setUser({ email: updated.email, username: updated.username })
      setProfileMessage('Your profile has been updated.')
    } catch (requestError) {
      setProfileError(
        requestError instanceof Error ? requestError.message : 'Unable to update your profile',
      )
    } finally {
      setSavingProfile(false)
    }
  }

  function submitProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void saveProfile()
  }

  function clearProfile() {
    setProfile(emptyProfile)
    void saveProfile(emptyProfile)
  }

  async function submitPassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!PASSWORD_PATTERN.test(newPassword)) {
      setPasswordError(PASSWORD_MESSAGE)
      return
    }
    setSavingPassword(true)
    setPasswordError('')
    setPasswordMessage('')
    try {
      await updateAccountPassword(currentPassword, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setPasswordMessage('Your password has been changed.')
    } catch (requestError) {
      setPasswordError(
        requestError instanceof Error ? requestError.message : 'Unable to change your password',
      )
    } finally {
      setSavingPassword(false)
    }
  }

  return (
    <div className="app-shell account-page">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Sillage home">
          <span className="brand__mark">S</span>
          <span className="brand__name">Sillage</span>
        </a>
        <nav aria-label="Primary navigation">
          <a href="/#discover">Discover</a>
          <a className="nav-favorite" href="/collection">
            <HeartIcon />
            My collection
          </a>
          <AuthControls />
        </nav>
      </header>

      <main className="account-page__main">
        <section className="account-page__heading">
          <span className="section-kicker">Your Sillage profile</span>
          <h1>My Account</h1>
          <p>Manage the details shown with your account and keep your password secure.</p>
        </section>

        {ready && !user && (
          <section className="account-page__gate">
            <h2>Log in to manage your account</h2>
            <button type="button" onClick={() => openAuth('login')}>Log in</button>
          </section>
        )}

        {user && (
          <div className="account-page__grid">
            <section className="account-card">
              <span className="section-kicker">Profile details</span>
              <h2>Your public profile</h2>
              {!loaded && !profileError && <p>Loading your account...</p>}
              <form onSubmit={submitProfile}>
                <label>
                  <span>Email</span>
                  <input type="email" value={user.email} disabled />
                </label>
                <label>
                  <span>Username</span>
                  <input
                    type="text"
                    value={profile.username}
                    minLength={2}
                    maxLength={40}
                    placeholder="Shown on the homepage"
                    onChange={(event) => setProfile((current) => ({
                      ...current,
                      username: event.target.value,
                    }))}
                  />
                </label>
                <label>
                  <span>Country</span>
                  <input
                    type="text"
                    value={profile.country}
                    maxLength={100}
                    onChange={(event) => setProfile((current) => ({
                      ...current,
                      country: event.target.value,
                    }))}
                  />
                </label>
                <label>
                  <span>City</span>
                  <input
                    type="text"
                    value={profile.city}
                    maxLength={100}
                    onChange={(event) => setProfile((current) => ({
                      ...current,
                      city: event.target.value,
                    }))}
                  />
                </label>
                {profileError && <p className="account-form__error" role="alert">{profileError}</p>}
                {profileMessage && <p className="account-form__success">{profileMessage}</p>}
                <div className="account-form__actions">
                  <button type="submit" disabled={!loaded || savingProfile}>
                    {savingProfile ? 'Saving...' : 'Save details'}
                  </button>
                  <button
                    type="button"
                    className="account-form__secondary"
                    disabled={savingProfile}
                    onClick={clearProfile}
                  >
                    Clear details
                  </button>
                </div>
              </form>
            </section>

            <section className="account-card">
              <span className="section-kicker">Security</span>
              <h2>Change password</h2>
              <form onSubmit={submitPassword}>
                <label>
                  <span>Current password</span>
                  <input
                    type="password"
                    value={currentPassword}
                    autoComplete="current-password"
                    required
                    maxLength={72}
                    onChange={(event) => setCurrentPassword(event.target.value)}
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
                <p className="account-form__help">{PASSWORD_MESSAGE}</p>
                {passwordError && (
                  <p className="account-form__error" role="alert">{passwordError}</p>
                )}
                {passwordMessage && <p className="account-form__success">{passwordMessage}</p>}
                <button type="submit" disabled={savingPassword}>
                  {savingPassword ? 'Updating...' : 'Update password'}
                </button>
              </form>
            </section>
          </div>
        )}
      </main>
    </div>
  )
}
