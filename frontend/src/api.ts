import type {
  Fragrance,
  FragranceFacets,
  FragranceFilters,
  FragranceImageBatch,
  FragranceMedia,
  AuthUser,
  AccountProfile,
  FragranceComment,
} from './types'
import { getLocalNoteImageUrl } from './localNoteImages'

async function getJson<T>(url: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(url, { signal })
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new Error(problem?.detail ?? `Request failed with status ${response.status}`)
  }
  return response.json() as Promise<T>
}

export function getFragrances(
  filters: FragranceFilters,
  signal?: AbortSignal,
): Promise<Fragrance[]> {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => params.append(
        key === 'manufacturers' ? 'manufacturer' : key,
        item,
      ))
    } else if (value.trim()) {
      params.set(key, value.trim())
    }
  })

  const query = params.toString()
  return getJson<Fragrance[]>(`/api/fragrances${query ? `?${query}` : ''}`, signal)
}

export function getFacets(signal?: AbortSignal): Promise<FragranceFacets> {
  return getJson<FragranceFacets>('/api/fragrances/facets', signal)
}

async function authRequest(
  path: string,
  options?: RequestInit,
): Promise<AuthUser> {
  const response = await fetch(`/api/auth/${path}`, {
    ...options,
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new Error(problem?.detail ?? `Request failed with status ${response.status}`)
  }
  return response.json() as Promise<AuthUser>
}

export async function getCurrentUser(signal?: AbortSignal): Promise<AuthUser | null> {
  const response = await fetch('/api/auth/me', {
    credentials: 'same-origin',
    signal,
  })
  if (response.status === 401) {
    return null
  }
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }
  return response.json() as Promise<AuthUser>
}

export function registerUser(email: string, password: string): Promise<AuthUser> {
  return authRequest('register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function loginUser(email: string, password: string): Promise<AuthUser> {
  return authRequest('login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function logoutUser(): Promise<void> {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'same-origin',
  })
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }
}

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function collectionRequest(
  path = '',
  options?: RequestInit,
): Promise<Response> {
  const response = await fetch(`/api/collection${path}`, {
    ...options,
    credentials: 'same-origin',
  })
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new ApiError(
      response.status,
      problem?.detail ?? `Request failed with status ${response.status}`,
    )
  }
  return response
}

export async function getCollection(signal?: AbortSignal): Promise<string[]> {
  const response = await collectionRequest('', { signal })
  const collection = await response.json() as { fragranceIds: string[] }
  return collection.fragranceIds
}

export async function addToCollection(fragranceId: string): Promise<void> {
  await collectionRequest(`/${encodeURIComponent(fragranceId)}`, { method: 'POST' })
}

export async function removeFromCollection(fragranceId: string): Promise<void> {
  await collectionRequest(`/${encodeURIComponent(fragranceId)}`, { method: 'DELETE' })
}

async function accountRequest(path = '', options?: RequestInit): Promise<Response> {
  const response = await fetch(`/api/account${path}`, {
    ...options,
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new ApiError(
      response.status,
      problem?.detail ?? `Request failed with status ${response.status}`,
    )
  }
  return response
}

export async function getAccountProfile(signal?: AbortSignal): Promise<AccountProfile> {
  const response = await accountRequest('', { signal })
  return response.json() as Promise<AccountProfile>
}

export async function updateAccountProfile(profile: {
  username: string
  country: string
  city: string
}): Promise<AccountProfile> {
  const response = await accountRequest('', {
    method: 'PUT',
    body: JSON.stringify(profile),
  })
  return response.json() as Promise<AccountProfile>
}

export async function updateAccountPassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await accountRequest('/password', {
    method: 'PUT',
    body: JSON.stringify({ currentPassword, newPassword }),
  })
}

async function commentRequest(
  fragranceId: string,
  path = '',
  options?: RequestInit,
): Promise<Response> {
  const response = await fetch(
    `/api/fragrances/${encodeURIComponent(fragranceId)}/comments${path}`,
    {
      ...options,
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    },
  )
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new ApiError(
      response.status,
      problem?.detail ?? `Request failed with status ${response.status}`,
    )
  }
  return response
}

export async function getFragranceComments(
  fragranceId: string,
  signal?: AbortSignal,
): Promise<FragranceComment[]> {
  const response = await commentRequest(fragranceId, '', { signal })
  return response.json() as Promise<FragranceComment[]>
}

export async function createFragranceComment(
  fragranceId: string,
  content: string,
): Promise<FragranceComment> {
  const response = await commentRequest(fragranceId, '', {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
  return response.json() as Promise<FragranceComment>
}

export async function updateFragranceComment(
  fragranceId: string,
  commentId: number,
  content: string,
): Promise<FragranceComment> {
  const response = await commentRequest(fragranceId, `/${commentId}`, {
    method: 'PUT',
    body: JSON.stringify({ content }),
  })
  return response.json() as Promise<FragranceComment>
}

export async function deleteFragranceComment(
  fragranceId: string,
  commentId: number,
): Promise<void> {
  await commentRequest(fragranceId, `/${commentId}`, { method: 'DELETE' })
}

export function getFragrance(id: string, signal?: AbortSignal): Promise<Fragrance> {
  return getJson<Fragrance>(`/api/fragrances/${encodeURIComponent(id)}`, signal)
}

export async function getFragranceMedia(
  fragrance: Fragrance,
  signal?: AbortSignal,
): Promise<FragranceMedia> {
  const notes = [
    ...fragrance.pyramid.top,
    ...fragrance.pyramid.heart,
    ...fragrance.pyramid.base,
  ].filter((note) => getLocalNoteImageUrl(note) === null)
  const response = await fetch('/api/media', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: fragrance.name,
      manufacturer: fragrance.manufacturer,
      notes,
    }),
    signal,
  })

  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new Error(problem?.detail ?? `Media request failed with status ${response.status}`)
  }

  return response.json() as Promise<FragranceMedia>
}

export async function getFragranceImages(
  fragrances: Fragrance[],
  signal?: AbortSignal,
): Promise<FragranceImageBatch> {
  const response = await fetch('/api/media/fragrances', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fragrances: fragrances.map(({ id, name, manufacturer }) => ({
        id,
        name,
        manufacturer,
      })),
    }),
    signal,
  })

  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as
      | { detail?: string }
      | null
    throw new Error(problem?.detail ?? `Image request failed with status ${response.status}`)
  }

  return response.json() as Promise<FragranceImageBatch>
}
