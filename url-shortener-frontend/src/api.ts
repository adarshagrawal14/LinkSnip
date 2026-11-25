import axios from 'axios'

const apiBase = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/+$/, '')}/api/v1`
  : '/api/v1'

const api = axios.create({
  baseURL: apiBase,
  headers: { 'Content-Type': 'application/json' },
})

// ─── Types ───────────────────────────────────────────────────────────────────

export interface ShortUrlResponse {
  shortCode: string
  shortUrl: string
  longUrl: string
  createdAt?: string
  expiresAt?: string
}

// ─── URLs ─────────────────────────────────────────────────────────────────────

export const createShortUrl = (longUrl: string, customAlias?: string, expiresAt?: string) =>
  api
    .post<ShortUrlResponse>('/urls', { longUrl, customAlias, expiresAt })
    .then((r) => r.data)

export default api

