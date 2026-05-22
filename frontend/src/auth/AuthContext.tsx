import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/client'
import type { AuthResponse } from '../api/types'

type AuthState = {
  token: string | null
  username: string | null
  roles: string[]
  expiresAt: number | null
}

type AuthContextValue = AuthState & {
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const storageKey = 'modweave-auth'

function loadStoredAuth(): AuthState {
  const raw = localStorage.getItem(storageKey)
  if (!raw) {
    return { token: null, username: null, roles: [], expiresAt: null }
  }
  try {
    const parsed = JSON.parse(raw) as AuthState
    return {
      token: parsed.token ?? null,
      username: parsed.username ?? null,
      roles: parsed.roles ?? [],
      expiresAt: parsed.expiresAt ?? null,
    }
  } catch {
    return { token: null, username: null, roles: [], expiresAt: null }
  }
}

function persistAuth(state: AuthState) {
  if (!state.token) {
    localStorage.removeItem(storageKey)
    return
  }
  localStorage.setItem(storageKey, JSON.stringify(state))
}

function toState(response: AuthResponse): AuthState {
  return {
    token: response.accessToken,
    username: response.username,
    roles: response.roles,
    expiresAt: Date.now() + response.expiresInSeconds * 1000,
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(() => loadStoredAuth())

  useEffect(() => {
    if (state.expiresAt && state.expiresAt < Date.now()) {
      setState({ token: null, username: null, roles: [], expiresAt: null })
      localStorage.removeItem(storageKey)
    }
  }, [state.expiresAt])

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginRequest(username, password)
    const nextState = toState(response)
    setState(nextState)
    persistAuth(nextState)
  }, [])

  const logout = useCallback(() => {
    setState({ token: null, username: null, roles: [], expiresAt: null })
    localStorage.removeItem(storageKey)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      login,
      logout,
      isAuthenticated: Boolean(state.token),
    }),
    [state, login, logout],
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
