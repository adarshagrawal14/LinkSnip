import { useState } from 'react'
import { createShortUrl, type ShortUrlResponse } from '../api'
import QrModal from '../components/QrModal'

function isValidUrl(str: string) {
  try { return Boolean(new URL(str)) } catch { return false }
}

export default function HomePage() {
  const [url, setUrl] = useState('')
  const [alias, setAlias] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<ShortUrlResponse | null>(null)
  const [error, setError] = useState('')
  const [copied, setCopied] = useState(false)
  const [showQr, setShowQr] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setResult(null)
    if (!isValidUrl(url)) { setError('Please enter a valid URL (include https://)'); return }

    setLoading(true)
    try {
      const data = await createShortUrl(url, alias || undefined)
      setResult(data)
      setUrl('')
      setAlias('')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const copyToClipboard = () => {
    if (!result) return
    navigator.clipboard.writeText(result.shortUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <main className="min-h-screen flex flex-col items-center justify-center px-4 py-16 bg-gradient-to-br from-brand-50 via-white to-indigo-50">
      {showQr && result && <QrModal url={result.shortUrl} onClose={() => setShowQr(false)} />}
      {/* Hero */}
      <div className="text-center mb-10">
        <h1 className="text-5xl font-extrabold text-gray-900 mb-3 tracking-tight">
          Shorten any link,{' '}
          <span className="text-brand-500">instantly</span>
        </h1>
        <p className="text-lg text-gray-500 max-w-md mx-auto">
          Paste a long URL and get a clean, shareable short link in seconds.
        </p>
      </div>

      {/* Form Card */}
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-xl p-8 border border-gray-100">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Long URL
            </label>
            <input
              type="url"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://example.com/very/long/link"
              required
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent text-sm transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Custom alias{' '}
              <span className="text-gray-400 font-normal">(optional)</span>
            </label>
            <div className="flex items-center rounded-lg border border-gray-300 focus-within:ring-2 focus-within:ring-brand-500 focus-within:border-transparent overflow-hidden transition">
              <span className="px-3 py-2.5 bg-gray-50 text-gray-400 text-sm border-r border-gray-300 select-none whitespace-nowrap">
                {typeof window !== 'undefined' ? window.location.host : 'linksnip'}/
              </span>
              <input
                type="text"
                value={alias}
                onChange={(e) => setAlias(e.target.value)}
                placeholder="my-link"
                className="flex-1 px-3 py-2.5 text-sm focus:outline-none"
              />
            </div>
          </div>

          {error && (
            <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-2.5">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 bg-brand-500 hover:bg-brand-600 disabled:opacity-60 text-white font-semibold rounded-lg transition text-sm flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                </svg>
                Shortening…
              </>
            ) : (
              'Shorten Link'
            )}
          </button>
        </form>

        {/* Result */}
        {result && (
          <div className="mt-6 p-4 bg-brand-50 border border-brand-100 rounded-xl animate-in">
            <p className="text-xs text-brand-700 font-medium mb-1 uppercase tracking-wide">Your short link</p>
            <div className="flex items-center gap-2">
              <a
                href={result.shortUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex-1 text-brand-600 font-semibold text-base hover:underline truncate"
              >
                {result.shortUrl}
              </a>
              <button
                onClick={copyToClipboard}
                className="shrink-0 px-3 py-1.5 bg-brand-500 hover:bg-brand-600 text-white text-xs rounded-lg transition font-medium"
              >
                {copied ? '✓ Copied!' : 'Copy'}
              </button>
              <button
                onClick={() => setShowQr(true)}
                title="Show QR Code"
                className="shrink-0 px-3 py-1.5 bg-white border border-brand-200 hover:bg-brand-50 text-brand-600 text-xs rounded-lg transition font-medium flex items-center gap-1"
              >
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
                </svg>
                QR
              </button>
            </div>
            <p className="text-xs text-gray-400 mt-2 truncate">
              → {result.longUrl}
            </p>
          </div>
        )}
      </div>

      {/* Features */}
      <div className="mt-14 grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-3xl w-full text-center">
        {[
          { icon: '⚡', title: 'Lightning fast', desc: 'Backed by Redis caching for instant redirects' },
          { icon: '🔒', title: 'Secure', desc: 'JWT auth & rate limiting keep your links safe' },
          { icon: '📊', title: 'Analytics', desc: 'Track clicks, referrers, and geographic data' },
        ].map((f) => (
          <div key={f.title} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
            <div className="text-3xl mb-2">{f.icon}</div>
            <h3 className="font-semibold text-gray-800">{f.title}</h3>
            <p className="text-sm text-gray-500 mt-1">{f.desc}</p>
          </div>
        ))}
      </div>
    </main>
  )
}
