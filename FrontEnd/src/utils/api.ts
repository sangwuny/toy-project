import { getAccessToken, loadSession } from './auth'

const API_BASE = (import.meta.env.VITE_API_BASE ?? '').replace(/\/+$/, '')

export const apiFetch = async (path: string, init: RequestInit = {}) => {
  const token = getAccessToken()
  const tokenType = loadSession()?.tokenType ?? 'Bearer'
  const headers = new Headers(init.headers ?? {})

  if (token) {
    headers.set('Authorization', `${tokenType} ${token}`)
  }

  return fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    ...init,
    headers,
  })
}
