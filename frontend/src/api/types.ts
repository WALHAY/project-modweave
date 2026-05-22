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

export type Version = {
  name: string
  changes?: string | null
  uploadDate: string
}

export type UserProfile = {
  name: string
  registerDate: string
}

export type TokenResponse = {
  accessToken: string
  refreshToken: string
  tokenType: string
}
