import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'CMS Ops – Daily Display',
  description: 'Day-by-day operational overview for scientific facility',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
