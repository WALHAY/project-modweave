import type {
  Category,
  Collection,
  Comment,
  Game,
  Mod,
  Page,
  TokenResponse,
  UserProfile,
  Version,
} from './types'

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

export async function getModVersionsPage(params: {
  modId: string
  page?: number
  size?: number
  signal?: AbortSignal
}) {
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 20,
  })
  return apiFetch<Page<Version>>(`/mods/${params.modId}/versions?${query}`, {
    signal: params.signal,
  })
}

export async function getUserProfile(username: string, signal?: AbortSignal) {
  return apiFetch<UserProfile>(`/users/${username}`, { signal })
}

export async function getUserMods(username: string, signal?: AbortSignal) {
  const query = buildQuery({ page: 0, size: 8 })
  return apiFetch<Page<Mod>>(`/users/${username}/mods?${query}`, { signal })
}

export async function getUsers(params: {
  page?: number
  size?: number
  username?: string
  signal?: AbortSignal
}) {
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 20,
    username: params.username,
  })
  return apiFetch<Page<UserProfile>>(`/users?${query}`, { signal: params.signal })
}

export async function createUser(params: {
  username: string
  name: string
  password: string
  email: string
}) {
  const body = new URLSearchParams()
  body.set('username', params.username)
  body.set('name', params.name)
  body.set('password', params.password)
  body.set('email', params.email)
  return apiFetch<UserProfile>('/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
}

export async function updateCurrentUser(params: {
  username?: string
  password?: string
  email?: string
  token?: string | null
}) {
  const body = new URLSearchParams()
  if (params.username) body.set('username', params.username)
  if (params.password) body.set('password', params.password)
  if (params.email) body.set('email', params.email)
  return apiFetch<UserProfile>('/users', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    token: params.token,
  })
}

export async function getCollection(collectionId: number, signal?: AbortSignal) {
  return apiFetch<Collection>(`/collections/${collectionId}`, { signal })
}

export async function getCollectionsByOwner(token?: string | null, signal?: AbortSignal) {
  const query = buildQuery({ owner: 'true' })
  return apiFetch<Collection[]>(`/collections?${query}`, { token, signal })
}

export async function searchCollectionsByName(name: string, signal?: AbortSignal) {
  const query = buildQuery({ name })
  return apiFetch<Collection[]>(`/collections?${query}`, { signal })
}

export async function getCollectionMods(params: {
  collectionId: number
  page?: number
  size?: number
  signal?: AbortSignal
}) {
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 20,
  })
  return apiFetch<Page<Mod>>(`/collections/${params.collectionId}/mods?${query}`, {
    signal: params.signal,
  })
}

export async function createCollection(params: {
  name: string
  description?: string
  token?: string | null
}) {
  const body = new URLSearchParams()
  body.set('name', params.name)
  if (params.description) body.set('description', params.description)
  return apiFetch<Collection>('/collections', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    token: params.token,
  })
}

export async function addModToCollection(params: {
  collectionId: number
  modId: string
  index?: number
  token?: string | null
}) {
  const query = buildQuery({ modId: params.modId, index: params.index })
  return apiFetch<Collection>(`/collections/${params.collectionId}?${query}`, {
    method: 'PUT',
    token: params.token,
  })
}

export async function deleteCollection(params: {
  collectionId: number
  token?: string | null
}) {
  return apiFetch<void>(`/collections/${params.collectionId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function deleteModFromCollection(params: {
  collectionId: number
  modId: string
  token?: string | null
}) {
  return apiFetch<void>(`/collections/${params.collectionId}/mods/${params.modId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function getComment(commentId: number, signal?: AbortSignal) {
  return apiFetch<Comment>(`/comments/${commentId}`, { signal })
}

export async function getCommentsForMod(modId: string, signal?: AbortSignal) {
  const query = buildQuery({ modId })
  return apiFetch<Comment[]>(`/comments?${query}`, { signal })
}

export async function createComment(params: {
  modId: string
  content: string
  token?: string | null
}) {
  const body = new URLSearchParams()
  body.set('modId', params.modId)
  body.set('content', params.content)
  return apiFetch<Comment>('/comments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    token: params.token,
  })
}

export async function deleteComment(params: { commentId: number; token?: string | null }) {
  return apiFetch<void>(`/comments/${params.commentId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function getModVersion(
  modId: string,
  versionId: string,
  signal?: AbortSignal
) {
  return apiFetch<Version>(`/mods/${modId}/versions/${versionId}`, { signal })
}

export async function createModVersion(params: {
  modId: string
  name: string
  changes?: string
  files: File[]
  token?: string | null
}) {
  const body = new FormData()
  body.append('name', params.name)
  if (params.changes) body.append('changes', params.changes)
  params.files.forEach((file) => body.append('files', file))
  return apiFetch<Version>(`/mods/${params.modId}/versions`, {
    method: 'POST',
    body,
    token: params.token,
  })
}

export async function uploadVersionFiles(params: {
  modId: string
  versionId: string
  files: File[]
  token?: string | null
}) {
  const body = new FormData()
  params.files.forEach((file) => body.append('files', file))
  return apiFetch<Version>(`/mods/${params.modId}/versions/${params.versionId}/files`, {
    method: 'POST',
    body,
    token: params.token,
  })
}

export async function deleteModVersion(params: {
  modId: string
  versionId: string
  token?: string | null
}) {
  return apiFetch<void>(`/mods/${params.modId}/versions/${params.versionId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function deleteCategory(params: { name: string; token?: string | null }) {
  const query = buildQuery({ categoryId: params.name })
  return apiFetch<void>(`/categories?${query}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function updateCategory(params: {
  name: string
  description?: string
  token?: string | null
}) {
  const body = new URLSearchParams()
  body.set('name', params.name)
  if (params.description) body.set('description', params.description)
  return apiFetch<Category>('/categories', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    token: params.token,
  })
}

export async function deleteGame(params: { gameId: string; token?: string | null }) {
  return apiFetch<void>(`/games/${params.gameId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export async function deleteMod(params: { modId: string; token?: string | null }) {
  return apiFetch<void>(`/mods/${params.modId}`, {
    method: 'DELETE',
    token: params.token,
  })
}

export function getFileDownloadUrl(bucket: string, fileId: string | number) {
  return `${baseUrl}/files/${bucket}/${fileId}/download`
}

export { baseUrl as apiBaseUrl }
