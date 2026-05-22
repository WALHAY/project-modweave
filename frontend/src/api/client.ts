import type { Category, Game, Mod, Page, TokenResponse, UserProfile, Version } from './types'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'

type RequestOptions = RequestInit & {
  token?: string | null
  signal?: AbortSignal
}

function buildQuery(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value))
    }
  })
  return search.toString()
}

async function apiFetch<T>(path: string, options: RequestOptions = {}) {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body && !headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (options.token) {
    headers.set('Authorization', `Bearer ${options.token}`)
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `Request failed with status ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export async function login(username: string, password: string) {
  const body = new URLSearchParams({ username, password }).toString()
  return apiFetch<TokenResponse>('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
}

export async function refreshToken(refreshToken: string) {
  const body = new URLSearchParams({ refreshToken }).toString()
  return apiFetch<TokenResponse>('/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
}

export async function getMods(params: {
  page?: number
  size?: number
  name?: string
  token?: string | null
  signal?: AbortSignal
}) {
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 12,
    name: params.name,
  })
  return apiFetch<Page<Mod>>(`/mods?${query}`, {
    token: params.token,
    signal: params.signal,
  })
}

export async function getMod(modId: string, signal?: AbortSignal) {
  return apiFetch<Mod>(`/mods/${modId}`, { signal })
}

export async function getGames(params: {
  page?: number
  size?: number
  name?: string
  signal?: AbortSignal
}) {
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 12,
    name: params.name,
  })
  return apiFetch<Page<Game>>(`/games?${query}`, { signal: params.signal })
}

export async function getGame(gameId: string, signal?: AbortSignal) {
  return apiFetch<Game>(`/games/${gameId}`, { signal })
}

export async function getCategories(signal?: AbortSignal) {
  return apiFetch<Category[]>('/categories', { signal })
}

export async function uploadCategory(params: {
  name: string
  description?: string
  token?: string | null
}) {
  const body = new URLSearchParams()
  body.set('name', params.name)
  if (params.description) {
    body.set('description', params.description)
  }
  return apiFetch<Category>('/categories', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    token: params.token,
  })
}

export async function uploadGame(params: {
  name: string
  description?: string
  image: File
  token?: string | null
}) {
  const body = new FormData()
  body.append('name', params.name)
  if (params.description) {
    body.append('description', params.description)
  }
  body.append('image', params.image)
  return apiFetch<Game>('/games', {
    method: 'POST',
    body,
    token: params.token,
  })
}

export async function uploadMod(params: {
  name: string
  description?: string
  image: File
  categories: string[]
  versionName: string
  files: File[]
  gameId: string
  token?: string | null
}) {
  const body = new FormData()
  body.append('name', params.name)
  if (params.description) {
    body.append('description', params.description)
  }
  body.append('image', params.image)
  params.categories.forEach((category) => body.append('categories', category))
  body.append('versionName', params.versionName)
  params.files.forEach((file) => body.append('files', file))
  body.append('gameId', params.gameId)
  return apiFetch<Mod>('/mods', {
    method: 'POST',
    body,
    token: params.token,
  })
}

export async function getModVersions(modId: string, signal?: AbortSignal) {
  const query = buildQuery({ page: 0, size: 5 })
  return apiFetch<Page<Version>>(`/mods/${modId}/versions?${query}`, { signal })
}

export async function getUserProfile(username: string, signal?: AbortSignal) {
  return apiFetch<UserProfile>(`/users/${username}`, { signal })
}

export async function getUserMods(username: string, signal?: AbortSignal) {
  const query = buildQuery({ page: 0, size: 8 })
  return apiFetch<Page<Mod>>(`/users/${username}/mods?${query}`, { signal })
}

export function getFileDownloadUrl(bucket: string, fileId: string | number) {
  return `${baseUrl}/files/${bucket}/${fileId}/download`
}

export { baseUrl as apiBaseUrl }
