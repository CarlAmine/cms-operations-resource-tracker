import { getDashboardSummary } from '../lib/api'
import { format } from 'date-fns'
import type { Booking, DailyNote, MaintenanceRecord } from '../types'
import clsx from 'clsx'

export const revalidate = 60 // Revalidate every 60s (ISR)

const statusColors: Record<string, string> = {
  AVAILABLE:   'bg-green-900 text-green-300 border border-green-700',
  BOOKED:      'bg-blue-900 text-blue-300 border border-blue-700',
  MAINTENANCE: 'bg-yellow-900 text-yellow-300 border border-yellow-700',
  UNAVAILABLE: 'bg-red-900 text-red-300 border border-red-700',
  APPROVED:    'bg-green-900 text-green-300',
  PENDING:     'bg-gray-800 text-gray-300',
  CANCELLED:   'bg-red-900 text-red-300',
  PLANNED:     'bg-blue-900 text-blue-300',
  IN_PROGRESS: 'bg-orange-900 text-orange-300',
  CRITICAL:    'bg-red-900 text-red-200 border border-red-600',
  HIGH:        'bg-orange-900 text-orange-200',
  NORMAL:      'bg-gray-800 text-gray-300',
  LOW:         'bg-gray-900 text-gray-500',
}

function Badge({ status }: { status: string }) {
  return (
    <span className={clsx('inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold', statusColors[status] ?? 'bg-gray-800 text-gray-300')}>
      {status.replace('_', ' ')}
    </span>
  )
}

function BookingCard({ booking }: { booking: Booking }) {
  return (
    <div className="bg-slate-800 border border-slate-700 rounded-lg p-4">
      <div className="flex justify-between items-start gap-2">
        <p className="font-semibold text-white">{booking.resourceName}</p>
        <Badge status={booking.status} />
      </div>
      <p className="text-slate-400 text-sm mt-1">{booking.purpose ?? 'No purpose specified'}</p>
      <div className="mt-2 flex gap-3 text-xs text-slate-500 font-mono">
        <span>{format(new Date(booking.startTime), 'HH:mm')}</span>
        <span>→</span>
        <span>{format(new Date(booking.endTime), 'HH:mm')}</span>
        <span className="text-slate-600">·</span>
        <span>{booking.bookedByUsername}</span>
      </div>
    </div>
  )
}

function NoteCard({ note }: { note: DailyNote }) {
  return (
    <div className={clsx(
      'bg-slate-800 border rounded-lg p-4',
      note.importance === 'CRITICAL' ? 'border-red-600' :
      note.importance === 'HIGH' ? 'border-orange-700' : 'border-slate-700'
    )}>
      <div className="flex items-start justify-between gap-2">
        <p className="font-semibold text-white">{note.title}</p>
        <Badge status={note.importance} />
      </div>
      <p className="text-slate-400 text-sm mt-2 leading-relaxed">{note.content}</p>
      <p className="text-slate-600 text-xs mt-2">— {note.authorUsername} · {note.category}</p>
    </div>
  )
}

function MaintenanceCard({ m }: { m: MaintenanceRecord }) {
  return (
    <div className="bg-slate-800 border border-yellow-800 rounded-lg p-4">
      <div className="flex justify-between gap-2">
        <p className="font-semibold text-white">{m.title}</p>
        <Badge status={m.status} />
      </div>
      <p className="text-slate-400 text-sm mt-1">{m.resourceName}</p>
      {m.scheduledEnd && (
        <p className="text-slate-500 text-xs mt-1 font-mono">
          Until: {format(new Date(m.scheduledEnd), 'MMM d, HH:mm')}
        </p>
      )}
    </div>
  )
}

export default async function DayDisplayPage() {
  const data = await getDashboardSummary()
  const now = new Date()

  return (
    <main className="min-h-screen bg-slate-900 p-6">
      {/* Header */}
      <header className="mb-8 flex items-center justify-between">
        <div>
          <p className="text-slate-500 text-sm uppercase tracking-widest font-semibold">Operations Overview</p>
          <h1 className="text-3xl font-bold text-white mt-1">{format(now, 'EEEE, MMMM d')}</h1>
          <p className="text-slate-400 text-sm mt-0.5">{format(now, 'yyyy')}</p>
        </div>
        <div className="text-right">
          <p className="text-4xl font-mono font-bold text-white">{format(now, 'HH:mm')}</p>
          <p className="text-slate-400 text-sm mt-1">Local time</p>
        </div>
      </header>

      {/* KPI row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Resources', value: data.totalResources, color: 'text-white' },
          { label: 'Available', value: data.availableResources, color: 'text-green-400' },
          { label: "Today's Bookings", value: data.activeBookingsToday, color: 'text-blue-400' },
          { label: 'Under Maintenance', value: data.resourcesUnderMaintenance, color: 'text-yellow-400' },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-slate-800 border border-slate-700 rounded-xl p-5">
            <p className="text-slate-500 text-xs uppercase tracking-wider">{label}</p>
            <p className={`text-4xl font-bold mt-2 ${color}`}>{value}</p>
          </div>
        ))}
      </div>

      {/* Main grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Today's Bookings */}
        <div className="lg:col-span-1">
          <h2 className="text-slate-400 text-xs uppercase tracking-widest font-semibold mb-3">
            Today's Bookings ({data.todaysBookings.length})
          </h2>
          <div className="space-y-3">
            {data.todaysBookings.length === 0 ? (
              <p className="text-slate-600 text-sm">No bookings today.</p>
            ) : (
              data.todaysBookings.map(b => <BookingCard key={b.id} booking={b} />)
            )}
          </div>
        </div>

        {/* Shift Notes */}
        <div className="lg:col-span-1">
          <h2 className="text-slate-400 text-xs uppercase tracking-widest font-semibold mb-3">
            Shift Notes ({data.todaysNotes.length})
          </h2>
          <div className="space-y-3">
            {data.todaysNotes.length === 0 ? (
              <p className="text-slate-600 text-sm">No notes for today.</p>
            ) : (
              data.todaysNotes.map(n => <NoteCard key={n.id} note={n} />)
            )}
          </div>
        </div>

        {/* Maintenance Alerts */}
        <div className="lg:col-span-1">
          <h2 className="text-slate-400 text-xs uppercase tracking-widest font-semibold mb-3">
            Active Maintenance ({data.activeMaintenances.length})
          </h2>
          <div className="space-y-3">
            {data.activeMaintenances.length === 0 ? (
              <p className="text-slate-600 text-sm">No active maintenance.</p>
            ) : (
              data.activeMaintenances.map(m => <MaintenanceCard key={m.id} m={m} />)
            )}
          </div>
        </div>
      </div>

      <footer className="mt-12 text-center text-slate-700 text-xs">
        CMS Operations Resource Tracker · Refreshes every 60s · {format(now, 'HH:mm:ss')}
      </footer>
    </main>
  )
}
