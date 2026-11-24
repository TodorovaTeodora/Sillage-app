import { useEffect, useState, type FormEvent } from 'react'
import {
  ApiError,
  createFragranceComment,
  deleteFragranceComment,
  getFragranceComments,
  updateFragranceComment,
} from '../api'
import { useAuth } from '../auth-context'
import type { FragranceComment } from '../types'

const EMOJIS = ['❤️', '😍', '✨', '🌸', '🍋', '🌹', '🌙', '🔥']

type CommentsState = {
  key: string
  comments: FragranceComment[]
  error: string
}

function formattedDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(value))
}

export function CommentsSection({ fragranceId }: { fragranceId: string }) {
  const { user, setUser, openAuth } = useAuth()
  const [state, setState] = useState<CommentsState>({
    key: '',
    comments: [],
    error: '',
  })
  const [draft, setDraft] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editText, setEditText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const commentsKey = `${fragranceId}:${user?.email ?? 'guest'}`
  const loading = state.key !== commentsKey

  useEffect(() => {
    const controller = new AbortController()
    getFragranceComments(fragranceId, controller.signal)
      .then((comments) => setState({ key: commentsKey, comments, error: '' }))
      .catch((requestError: Error) => {
        if (requestError.name !== 'AbortError') {
          setState({
            key: commentsKey,
            comments: [],
            error: requestError.message,
          })
        }
      })
    return () => controller.abort()
  }, [commentsKey, fragranceId])

  function requireLogin() {
    openAuth('login', 'Please log in or register to join the fragrance discussion.')
  }

  function handleRequestError(requestError: unknown) {
    if (requestError instanceof ApiError && requestError.status === 401) {
      setUser(null)
      requireLogin()
      return
    }
    setFormError(
      requestError instanceof Error ? requestError.message : 'Unable to update the comment',
    )
  }

  async function submitComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!user) {
      requireLogin()
      return
    }
    if (!draft.trim()) return

    setSubmitting(true)
    setFormError('')
    try {
      const comment = await createFragranceComment(fragranceId, draft)
      setState((current) => ({
        ...current,
        comments: [...current.comments, comment],
      }))
      setDraft('')
    } catch (requestError) {
      handleRequestError(requestError)
    } finally {
      setSubmitting(false)
    }
  }

  async function saveEdit(commentId: number) {
    if (!editText.trim()) return
    setSubmitting(true)
    setFormError('')
    try {
      const updated = await updateFragranceComment(fragranceId, commentId, editText)
      setState((current) => ({
        ...current,
        comments: current.comments.map((comment) =>
          comment.id === commentId ? updated : comment),
      }))
      setEditingId(null)
      setEditText('')
    } catch (requestError) {
      handleRequestError(requestError)
    } finally {
      setSubmitting(false)
    }
  }

  async function removeComment(commentId: number) {
    if (!window.confirm('Delete this comment?')) return
    setSubmitting(true)
    setFormError('')
    try {
      await deleteFragranceComment(fragranceId, commentId)
      setState((current) => ({
        ...current,
        comments: current.comments.filter((comment) => comment.id !== commentId),
      }))
    } catch (requestError) {
      handleRequestError(requestError)
    } finally {
      setSubmitting(false)
    }
  }

  function emojiBar(append: (emoji: string) => void) {
    return (
      <div className="comments__emoji-bar" aria-label="Add an emoji">
        {EMOJIS.map((emoji) => (
          <button
            type="button"
            aria-label={`Add ${emoji}`}
            onClick={() => append(emoji)}
            key={emoji}
          >
            {emoji}
          </button>
        ))}
      </div>
    )
  }

  return (
    <section className="comments">
      <div className="comments__heading">
        <div>
          <span className="section-kicker">Community</span>
          <h2>Fragrance comments</h2>
        </div>
        <span>{state.comments.length} {state.comments.length === 1 ? 'comment' : 'comments'}</span>
      </div>

      {user ? (
        <form className="comments__composer" onSubmit={submitComment}>
          <label htmlFor="new-comment">Share your experience</label>
          <textarea
            id="new-comment"
            value={draft}
            maxLength={1000}
            placeholder="How does this fragrance wear on you?"
            onChange={(event) => setDraft(event.target.value)}
          />
          <div className="comments__composer-footer">
            {emojiBar((emoji) => setDraft((current) => `${current}${emoji}`))}
            <span>{draft.length}/1000</span>
            <button type="submit" disabled={submitting || !draft.trim()}>
              Post comment
            </button>
          </div>
        </form>
      ) : (
        <div className="comments__login">
          <p>Log in to write a comment and join the discussion.</p>
          <button type="button" onClick={requireLogin}>Log in to comment</button>
        </div>
      )}

      {formError && <p className="comments__error" role="alert">{formError}</p>}
      {loading && <p className="comments__status">Loading comments...</p>}
      {!loading && state.error && (
        <p className="comments__error" role="alert">{state.error}</p>
      )}
      {!loading && !state.error && state.comments.length === 0 && (
        <p className="comments__status">Be the first to share your thoughts.</p>
      )}

      {!loading && state.comments.length > 0 && (
        <div className="comments__list">
          {state.comments.map((comment) => (
            <article className="comment" key={comment.id}>
              <header>
                <span className="comment__avatar">
                  {comment.author.charAt(0).toUpperCase()}
                </span>
                <div>
                  <strong>{comment.author}</strong>
                  <small>
                    {formattedDate(comment.createdAt)}
                    {comment.updatedAt !== comment.createdAt && ' · Edited'}
                  </small>
                </div>
              </header>

              {editingId === comment.id ? (
                <div className="comment__edit">
                  <textarea
                    value={editText}
                    maxLength={1000}
                    aria-label="Edit comment"
                    onChange={(event) => setEditText(event.target.value)}
                  />
                  {emojiBar((emoji) => setEditText((current) => `${current}${emoji}`))}
                  <div className="comment__actions">
                    <button
                      type="button"
                      disabled={submitting || !editText.trim()}
                      onClick={() => void saveEdit(comment.id)}
                    >
                      Save
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setEditingId(null)
                        setEditText('')
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <p>{comment.content}</p>
              )}

              {comment.ownedByCurrentUser && editingId !== comment.id && (
                <div className="comment__actions">
                  <button
                    type="button"
                    onClick={() => {
                      setEditingId(comment.id)
                      setEditText(comment.content)
                    }}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    disabled={submitting}
                    onClick={() => void removeComment(comment.id)}
                  >
                    Delete
                  </button>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
