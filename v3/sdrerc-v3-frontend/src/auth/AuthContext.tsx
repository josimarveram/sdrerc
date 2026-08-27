import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import type { SessionResponse } from '../api/authApi'

export interface Session {
  accessToken: string
  username: string
  nombreCompleto: string
  roles: string[]
  permisos: string[]
}

interface AuthContextValue {
  session: Session | null
  setSession: (session: SessionResponse) => void
  logout: () => void
}

const STORAGE_KEY = 'sdrerc.v3.session'

// sessionStorage (no localStorage): la sesion no debe sobrevivir mas alla de la pestana/ventana,
// coherente con que el accessToken vence en minutos (ver JwtService, sesion 15-30 min por diseno,
// sin refresh token todavia - plan de migracion, seccion 2).
function readStoredSession(): Session | null {
  const raw = sessionStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as Session
    // Migracion defensiva: una sesion guardada antes de agregar `permisos` (20/08/2026) no trae ese
    // campo - sin este fallback, session.permisos.length revienta con "Cannot read properties of
    // undefined" hasta que el usuario vuelve a loguearse. Fail-open (lista vacia) es el mismo criterio
    // que ya se aplica cuando el backend SI devuelve `permisos: []`.
    return { ...parsed, permisos: parsed.permisos ?? [] }
  } catch {
    return null
  }
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<Session | null>(() => readStoredSession())

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      setSession: (response: SessionResponse) => {
        const next: Session = {
          accessToken: response.accessToken,
          username: response.username,
          nombreCompleto: response.nombreCompleto,
          roles: response.roles,
          permisos: response.permisos,
        }
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next))
        setSessionState(next)
      },
      logout: () => {
        sessionStorage.removeItem(STORAGE_KEY)
        setSessionState(null)
      },
    }),
    [session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>.')
  }
  return ctx
}
