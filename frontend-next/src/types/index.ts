// Shared types (subset of backend API responses)
export interface Booking {
  id: number
  resourceId: number
  resourceName: string
  bookedByUsername: string
  startTime: string
  endTime: string
  purpose?: string
  status: string
}

export interface DailyNote {
  id: number
  noteDate: string
  authorUsername: string
  title: string
  content: string
  category: string
  importance: string
  pinned: boolean
  createdAt: string
}

export interface MaintenanceRecord {
  id: number
  resourceId: number
  resourceName: string
  title: string
  status: string
  scheduledStart: string
  scheduledEnd?: string
}

export interface DashboardSummary {
  totalResources: number
  availableResources: number
  resourcesUnderMaintenance: number
  activeBookingsToday: number
  pendingBookings: number
  todaysBookings: Booking[]
  todaysNotes: DailyNote[]
  activeMaintenances: MaintenanceRecord[]
  resourcesByStatus: Record<string, number>
  resourcesByCategory: Record<string, number>
}
