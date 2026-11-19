import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api } from './api'

const AuthContext = createContext(null)

export const ROLE_HOME = {
  FARMER: '/farmer',
  BUYER: '/buyer',
  ADMIN: '/admin',
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Restore the session (if any) when the app loads.
  useEffect(() => {
    let cancelled = false
    api('/api/auth/me')
      .then((u) => !cancelled && setUser(u))
      .catch(() => !cancelled && setUser(null))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (email, password) => {
    const u = await api('/api/auth/login', { method: 'POST', body: { email, password } })
    setUser(u)
    return u
  }, [])

  const register = useCallback(
    (payload) => api('/api/auth/register', { method: 'POST', body: payload }),
    []
  )

  const logout = useCallback(async () => {
    try {
      await api('/api/auth/logout', { method: 'POST' })
    } finally {
      setUser(null)
    }
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
