export type Page<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type Mod = {
  id: string
  name: string
  description?: string | null
  imagePath: string
  creationDate: string
  publisherId: string
  gameId: string
  categories: string[]
}

export type Game = {
  id: string
  name: string
  description?: string | null
  imagePath: string
}

export type Category = {
  name: string
  description?: string | null
}

export type Version = {
  id?: string | number
  name: string
  changes?: string | null
  uploadDate: string
  files?: VersionFile[]
}

export type VersionFile = {
  id: string | number
  filename: string
  filePath: string
}

export type UserProfile = {
  name: string
  registerDate: string
}

export type Collection = {
  id: number
  name: string
  description?: string | null
  ownerId: string
}

export type Comment = {
  id: number
  content: string
  publishDate: string
  authorId: string
  modId: string
}

export type TokenResponse = {
  accessToken: string
  refreshToken: string
  tokenType: string
}
