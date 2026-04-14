export type Role = 'ADMIN' | 'COORDINATOR' | 'OPERATOR' | 'VIEWER'

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: Role
  enabled: boolean
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: User
}

export type ResourceStatus = 'AVAILABLE' | 'BOOKED' | 'MAINTENANCE' | 'UNAVAILABLE'
export type ResourceCategory = 'DETECTOR' | 'COMPUTING' | 'NETWORKING' | 'CRYOGENICS' | 'ELECTRONICS' | 'MECHANICAL' | 'SAFETY' | 'OTHER'

export interface Resource {
  id: number
  name: string
  description?: string
  category: ResourceCategory
  status: ResourceStatus
  locationId?: number
  locationName?: string
  notes?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export type BookingStatus = 'PENDING' | 'APPROVED' | 'CANCELLED' | 'COMPLETED'

export interface Booking {
  id: number
  resourceId: number
  resourceName: string
  bookedById: number
  bookedByUsername: string
  startTime: string
  endTime: string
  purpose?: string
  status: BookingStatus
  approvedById?: number
  approvedByUsername?: string
  approvedAt?: string
  cancellationReason?: string
  createdAt: string
}

export type MaintenanceStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface MaintenanceRecord {
  id: number
  resourceId: number
  resourceName: string
  reportedById: number
  reportedByUsername: string
  title: string
  description?: string
  type: string
  scheduledStart: string
  scheduledEnd?: string
  actualEnd?: string
  status: MaintenanceStatus
  resolution?: string
  createdAt: string
}

export type NoteImportance = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL'

export interface DailyNote {
  id: number
  noteDate: string
  authorId: number
  authorUsername: string
  title: string
  content: string
  category: string
  importance: NoteImportance
  pinned: boolean
  createdAt: string
}

export interface AuditEvent {
  id: number
  eventType: string
  entityType: string
  entityId?: number
  performedByUsername: string
  description: string
  occurredAt: string
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

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
