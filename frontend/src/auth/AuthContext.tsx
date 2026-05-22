import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { login as loginRequest, refreshToken as refreshTokenRequest } from '../api/client'
import type { TokenResponse } from '../api/types'

type AuthState = {
  token: string | null
  refreshToken: string | null
  username: string | null
  roles: string[]
  expiresAt: number | null
}

type AuthContextValue = AuthState & {
  login: (username: string, password: string) => Promise<void>
  refresh: () => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const storageKey = 'modweave-auth'

function loadStoredAuth(): AuthState {
  const raw = localStorage.getItem(storageKey)
  if (!raw) {
    return { token: null, refreshToken: null, username: null, roles: [], expiresAt: null }
  }
  try {
    const parsed = JSON.parse(raw) as AuthState
    return {
      token: parsed.token ?? null,
      refreshToken: parsed.refreshToken ?? null,
      username: parsed.username ?? null,
      roles: parsed.roles ?? [],
      expiresAt: parsed.expiresAt ?? null,
    }
  } catch {
    return { token: null, refreshToken: null, username: null, roles: [], expiresAt: null }
  }
}

function persistAuth(state: AuthState) {
  if (!state.token) {
    localStorage.removeItem(storageKey)
    return
  }
  localStorage.setItem(storageKey, JSON.stringify(state))
}

type JwtPayload = {
  sub?: string
  roles?: string[]
  exp?: number
}

function decodeBase64Url(value: string) {
  const padded = value.padEnd(Math.ceil(value.length / 4) * 4, '=')
  return atob(padded.replace(/-/g, '+').replace(/_/g, '/'))
}

function parseJwt(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split('.')
    if (!payload) {
      return null
    }
    return JSON.parse(decodeBase64Url(payload)) as JwtPayload
  } catch {
    return null
  }
}

function toState(response: TokenResponse): AuthState {
  const payload = parseJwt(response.accessToken)
  return {
    token: response.accessToken,
    refreshToken: response.refreshToken,
    username: payload?.sub ?? null,
    roles: Array.isArray(payload?.roles) ? payload.roles : [],
    expiresAt: payload?.exp ? payload.exp * 1000 : null,
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(() => loadStoredAuth())
  const refreshInFlight = useRef<Promise<void> | null>(null)

  const refresh = useCallback(async () => {
    if (!state.refreshToken) {
      throw new Error('Missing refresh token')
    }
    if (!refreshInFlight.current) {
      refreshInFlight.current = refreshTokenRequest(state.refreshToken)
        .then((response) => {
          const nextState = toState(response)
          setState(nextState)
          persistAuth(nextState)
        })
        .catch((err) => {
          setState({ token: null, refreshToken: null, username: null, roles: [], expiresAt: null })
          localStorage.removeItem(storageKey)
          throw err
        })
        .finally(() => {
          refreshInFlight.current = null
        })
    }
    await refreshInFlight.current
  }, [state.refreshToken])

  useEffect(() => {
    if (state.expiresAt && state.expiresAt < Date.now() && state.refreshToken) {
      void refresh()
    } else if (state.expiresAt && state.expiresAt < Date.now()) {
      setState({ token: null, refreshToken: null, username: null, roles: [], expiresAt: null })
      localStorage.removeItem(storageKey)
    }
  }, [state.expiresAt, state.refreshToken, refresh])

  useEffect(() => {
    if (!state.expiresAt || !state.refreshToken) {
      return
    }
    const bufferMs = 60_000
    const delay = Math.max(state.expiresAt - Date.now() - bufferMs, 0)
    const timer = window.setTimeout(() => {
      void refresh()
    }, delay)
    return () => window.clearTimeout(timer)
  }, [state.expiresAt, state.refreshToken, refresh])

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginRequest(username, password)
    const nextState = toState(response)
    setState(nextState)
    persistAuth(nextState)
  }, [])

  const logout = useCallback(() => {
    setState({ token: null, refreshToken: null, username: null, roles: [], expiresAt: null })
    localStorage.removeItem(storageKey)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      login,
      refresh,
      logout,
      isAuthenticated: Boolean(state.token),
    }),
    [state, login, logout, refresh],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
