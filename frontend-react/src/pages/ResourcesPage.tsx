import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'
import type { Resource } from '../types'
import { PageLoader } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext'

export default function ResourcesPage() {
  const { data: resources, isLoading } = useQuery<Resource[]>({
    queryKey: ['resources'],
    queryFn: () => api.get('/api/resources').then(r => r.data),
  })
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      api.patch(`/api/resources/${id}/status`, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      toast.success('Resource status updated')
    },
    onError: () => toast.error('Failed to update status'),
  })

  const filtered = (resources ?? []).filter(r => {
    const matchesSearch = r.name.toLowerCase().includes(search.toLowerCase())
    const matchesStatus = !statusFilter || r.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const canEdit = user?.role !== 'VIEWER'

  if (isLoading) return <PageLoader />

  return (
    <div>
      <PageHeader
        title="Resources"
        subtitle={`${filtered.length} resources found`}
      />
      <div className="px-8 space-y-4">
        {/* Filters */}
        <div className="flex gap-3">
          <input
            className="input max-w-xs"
            placeholder="Search resources…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <select
            className="input max-w-[180px]"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
          >
            <option value="">All statuses</option>
            {['AVAILABLE','BOOKED','MAINTENANCE','UNAVAILABLE'].map(s => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>

        {/* Table */}
        {filtered.length === 0 ? (
          <EmptyState title="No resources found" description="Try adjusting your filters." />
        ) : (
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  {['Name','Category','Status','Location','Notes'].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                  {canEdit && <th className="px-4 py-3" />}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map(r => (
                  <tr key={r.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">{r.name}</td>
                    <td className="px-4 py-3 text-gray-500">{r.category}</td>
                    <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
                    <td className="px-4 py-3 text-gray-500">{r.locationName ?? '—'}</td>
                    <td className="px-4 py-3 text-gray-400 max-w-[200px] truncate">{r.notes ?? '—'}</td>
                    {canEdit && (
                      <td className="px-4 py-3">
                        <select
                          className="text-xs border border-gray-200 rounded px-2 py-1 bg-white"
                          value={r.status}
                          onChange={e => updateStatus.mutate({ id: r.id, status: e.target.value })}
                        >
                          {['AVAILABLE','BOOKED','MAINTENANCE','UNAVAILABLE'].map(s => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                      </td>
                    )}
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
