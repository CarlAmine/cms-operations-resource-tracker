import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'
import type { Booking } from '../types'
import { PageLoader } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { format } from 'date-fns'
import { useAuth } from '../context/AuthContext'

export default function BookingsPage() {
  const { data: bookings, isLoading } = useQuery<Booking[]>({
    queryKey: ['bookings'],
    queryFn: () => api.get('/api/bookings').then(r => r.data),
  })
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [statusFilter, setStatusFilter] = useState('')

  const approve = useMutation({
    mutationFn: (id: number) => api.post(`/api/bookings/${id}/approve`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['bookings'] }); toast.success('Booking approved') },
    onError: () => toast.error('Failed to approve booking'),
  })

  const cancel = useMutation({
    mutationFn: (id: number) => api.post(`/api/bookings/${id}/cancel`, { reason: 'Cancelled via dashboard' }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['bookings'] }); toast.success('Booking cancelled') },
    onError: () => toast.error('Failed to cancel booking'),
  })

  const canApprove = ['ADMIN','COORDINATOR'].includes(user?.role ?? '')

  const filtered = (bookings ?? []).filter(b => !statusFilter || b.status === statusFilter)

  if (isLoading) return <PageLoader />

  return (
    <div>
      <PageHeader title="Bookings" subtitle={`${filtered.length} bookings`} />
      <div className="px-8 space-y-4">
        <div className="flex gap-3">
          <select
            className="input max-w-[180px]"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
          >
            <option value="">All statuses</option>
            {['PENDING','APPROVED','CANCELLED','COMPLETED'].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        {filtered.length === 0 ? (
          <EmptyState title="No bookings found" />
        ) : (
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  {['Resource','Booked By','Start','End','Purpose','Status','Actions'].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map(b => (
                  <tr key={b.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{b.resourceName}</td>
                    <td className="px-4 py-3 text-gray-500">{b.bookedByUsername}</td>
                    <td className="px-4 py-3 text-gray-500 font-mono text-xs">{format(new Date(b.startTime), 'MMM d, HH:mm')}</td>
                    <td className="px-4 py-3 text-gray-500 font-mono text-xs">{format(new Date(b.endTime), 'MMM d, HH:mm')}</td>
                    <td className="px-4 py-3 text-gray-400 max-w-[160px] truncate">{b.purpose ?? '—'}</td>
                    <td className="px-4 py-3"><StatusBadge status={b.status} /></td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        {canApprove && b.status === 'PENDING' && (
                          <button
                            onClick={() => approve.mutate(b.id)}
                            className="text-xs bg-green-50 hover:bg-green-100 text-green-700 px-2 py-1 rounded"
                          >Approve</button>
                        )}
                        {b.status !== 'CANCELLED' && b.status !== 'COMPLETED' && (
                          <button
                            onClick={() => cancel.mutate(b.id)}
                            className="text-xs bg-red-50 hover:bg-red-100 text-red-700 px-2 py-1 rounded"
                          >Cancel</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
