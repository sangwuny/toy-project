export type AuthSession = {
  accessToken?: string
  tokenType?: string
  expiresAt?: number
  user?: {
    id?: string | number
    name?: string
    email?: string
  }
  sessionCookie?: boolean
}

const STORAGE_KEY = 'auth_session'

const readStorage = (storage: Storage | null) => {
  if (!storage) return null
  const raw = storage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthSession
  } catch {
    return null
  }
}

export const loadSession = () => readStorage(localStorage) ?? readStorage(sessionStorage)

export const saveSession = (session: AuthSession, remember: boolean) => {
  const target = remember ? localStorage : sessionStorage
  const other = remember ? sessionStorage : localStorage
  other.removeItem(STORAGE_KEY)
  target.setItem(STORAGE_KEY, JSON.stringify(session))
}

export const clearSession = () => {
  localStorage.removeItem(STORAGE_KEY)
  sessionStorage.removeItem(STORAGE_KEY)
}

export const getAccessToken = () => loadSession()?.accessToken ?? null

export const isAuthenticated = () => {
  const session = loadSession()
  if (!session) return false
  if (session.expiresAt && Date.now() > session.expiresAt) {
    clearSession()
    return false
  }
  return Boolean(session.accessToken || session.sessionCookie)
}
