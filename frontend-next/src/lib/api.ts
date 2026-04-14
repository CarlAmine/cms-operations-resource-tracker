import type { DashboardSummary } from '../types'

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080'

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const res = await fetch(`${BASE_URL}/api/dashboard/summary`, {
    next: { revalidate: 60 },
    headers: {
      // The Next.js display page is read-only and uses a service token if configured
      ...(process.env.DISPLAY_SERVICE_TOKEN
        ? { Authorization: `Bearer ${process.env.DISPLAY_SERVICE_TOKEN}` }
        : {}),
    },
  })

  if (!res.ok) {
    // Return empty fallback so the display page always renders
    return {
      totalResources: 0,
      availableResources: 0,
      resourcesUnderMaintenance: 0,
      activeBookingsToday: 0,
      pendingBookings: 0,
      todaysBookings: [],
      todaysNotes: [],
      activeMaintenances: [],
      resourcesByStatus: {},
      resourcesByCategory: {},
    }
  }

  return res.json()
}
