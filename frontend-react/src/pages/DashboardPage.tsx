import { useQuery } from '@tanstack/react-query'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import api from '../lib/api'
import type { DashboardSummary } from '../types'
import { PageLoader } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { format } from 'date-fns'

const STATUS_COLORS: Record<string, string> = {
  AVAILABLE: '#22c55e',
  BOOKED: '#3b82f6',
  MAINTENANCE: '#f59e0b',
  UNAVAILABLE: '#ef4444',
}

export default function DashboardPage() {
  const { data, isLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard'],
    queryFn: () => api.get('/api/dashboard/summary').then(r => r.data),
    refetchInterval: 60_000,
  })

  if (isLoading || !data) return <PageLoader />

  const statusChartData = Object.entries(data.resourcesByStatus).map(([name, value]) => ({ name, value }))
  const categoryChartData = Object.entries(data.resourcesByCategory).map(([name, value]) => ({ name, value }))

  return (
    <div className="px-8 py-8 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Operational Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">{format(new Date(), "EEEE, d MMMM yyyy")}</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Total Resources', value: data.totalResources, color: 'text-gray-900' },
          { label: 'Available', value: data.availableResources, color: 'text-green-600' },
          { label: "Today's Bookings", value: data.activeBookingsToday, color: 'text-blue-600' },
          { label: 'Pending Approval', value: data.pendingBookings, color: 'text-orange-600' },
        ].map(({ label, value, color }) => (
          <div key={label} className="card p-5">
            <p className="text-xs font-medium text-gray-500 uppercase tracking-wider">{label}</p>
            <p className={`text-3xl font-bold mt-2 ${color}`}>{value}</p>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="card p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Resources by Status</h2>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={statusChartData}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="value" radius={[4,4,0,0]}>
                {statusChartData.map((entry) => (
                  <Cell key={entry.name} fill={STATUS_COLORS[entry.name] ?? '#94a3b8'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Resources by Category</h2>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={categoryChartData}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="value" fill="#3b82f6" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Today's activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Today's notes */}
        <div className="card p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Shift Notes Today</h2>
          {data.todaysNotes.length === 0 ? (
            <p className="text-gray-400 text-sm">No notes for today.</p>
          ) : (
            <ul className="space-y-3">
              {data.todaysNotes.slice(0, 4).map(note => (
                <li key={note.id} className="flex gap-3">
                  <StatusBadge status={note.importance} />
                  <div>
                    <p className="text-sm font-medium text-gray-800">{note.title}</p>
                    <p className="text-xs text-gray-400">by {note.authorUsername}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Active maintenance */}
        <div className="card p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Active Maintenance</h2>
          {data.activeMaintenances.length === 0 ? (
            <p className="text-gray-400 text-sm">No active maintenance.</p>
          ) : (
            <ul className="space-y-3">
              {data.activeMaintenances.slice(0, 4).map(m => (
                <li key={m.id} className="flex gap-3 items-start">
                  <StatusBadge status={m.status} />
                  <div>
                    <p className="text-sm font-medium text-gray-800">{m.title}</p>
                    <p className="text-xs text-gray-400">{m.resourceName}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
