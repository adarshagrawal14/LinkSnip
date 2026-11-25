import { useRef } from 'react'
import { QRCodeCanvas } from 'qrcode.react'

interface Props {
  url: string
  onClose: () => void
}

export default function QrModal({ url, onClose }: Props) {
  const canvasRef = useRef<HTMLDivElement>(null)

  const downloadQr = () => {
    const canvas = canvasRef.current?.querySelector('canvas')
    if (!canvas) return
    const link = document.createElement('a')
    link.download = 'qrcode.png'
    link.href = canvas.toDataURL('image/png')
    link.click()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-2xl shadow-2xl p-8 flex flex-col items-center gap-5 w-80"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between w-full">
          <h2 className="font-bold text-gray-900 text-lg">QR Code</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition text-xl leading-none"
          >
            ✕
          </button>
        </div>

        {/* QR Code */}
        <div ref={canvasRef} className="p-3 bg-white border-2 border-gray-100 rounded-xl">
          <QRCodeCanvas
            value={url}
            size={200}
            bgColor="#ffffff"
            fgColor="#1a2a7a"
            level="H"
            marginSize={1}
          />
        </div>

        {/* URL label */}
        <p className="text-xs text-gray-400 text-center break-all max-w-full px-2">{url}</p>

        {/* Buttons */}
        <div className="flex gap-3 w-full">
          <button
            onClick={downloadQr}
            className="flex-1 flex items-center justify-center gap-2 py-2.5 bg-brand-500 hover:bg-brand-600 text-white text-sm font-semibold rounded-lg transition"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            Download PNG
          </button>
          <button
            onClick={onClose}
            className="flex-1 py-2.5 border border-gray-200 text-gray-600 hover:bg-gray-50 text-sm font-medium rounded-lg transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  )
}
