import type { AuthResponse, Mod, Page, UserProfile, Version } from './types'

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
  if (options.body && !headers.has('Content-Type')) {
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
  return apiFetch<AuthResponse>('/auth/login', {
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
