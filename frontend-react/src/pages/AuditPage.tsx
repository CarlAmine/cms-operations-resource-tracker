import { useQuery } from '@tanstack/react-query'
import api from '../lib/api'
import type { AuditEvent, Page } from '../types'
import { PageLoader } from '../components/Spinner'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { format } from 'date-fns'
import { useState } from 'react'

export default function AuditPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useQuery<Page<AuditEvent>>({
    queryKey: ['audit', page],
    queryFn: () => api.get('/api/audit', { params: { page, size: 20 } }).then(r => r.data),
  })

  if (isLoading) return <PageLoader />

  return (
    <div>
      <PageHeader title="Audit Log" subtitle="Track all significant system events" />
      <div className="px-8">
        {!data?.content.length ? (
          <EmptyState title="No audit events" />
        ) : (
          <>
            <div className="card overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    {['Event','Entity','Entity ID','Performed By','Description','Timestamp'].map(h => (
                      <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {data.content.map(e => (
                    <tr key={e.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <span className="font-mono text-xs bg-gray-100 px-2 py-0.5 rounded text-gray-700">{e.eventType}</span>
                      </td>
                      <td className="px-4 py-3 text-gray-500">{e.entityType}</td>
                      <td className="px-4 py-3 text-gray-400">{e.entityId ?? '—'}</td>
                      <td className="px-4 py-3 font-medium">{e.performedByUsername}</td>
                      <td className="px-4 py-3 text-gray-600 max-w-[240px] truncate">{e.description}</td>
                      <td className="px-4 py-3 font-mono text-xs text-gray-400">
                        {format(new Date(e.occurredAt), 'MMM d, HH:mm:ss')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="flex justify-between items-center mt-4 text-sm text-gray-500">
              <span>Page {page + 1} of {data.totalPages}</span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="btn-secondary disabled:opacity-40 text-sm"
                >Previous</button>
                <button
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= data.totalPages - 1}
                  className="btn-secondary disabled:opacity-40 text-sm"
                >Next</button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
