import { useQuery } from '@tanstack/react-query'
import api from '../lib/api'
import type { MaintenanceRecord } from '../types'
import { PageLoader } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { format } from 'date-fns'

export default function MaintenancePage() {
  const { data, isLoading } = useQuery<MaintenanceRecord[]>({
    queryKey: ['maintenance'],
    queryFn: () => api.get('/api/maintenance').then(r => r.data),
  })

  if (isLoading) return <PageLoader />

  return (
    <div>
      <PageHeader title="Maintenance" subtitle="Resource maintenance records" />
      <div className="px-8">
        {!data?.length ? (
          <EmptyState title="No maintenance records" />
        ) : (
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  {['Resource','Title','Type','Status','Scheduled Start','Scheduled End','Reporter'].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.map(m => (
                  <tr key={m.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{m.resourceName}</td>
                    <td className="px-4 py-3 text-gray-700">{m.title}</td>
                    <td className="px-4 py-3 text-gray-500">{m.type}</td>
                    <td className="px-4 py-3"><StatusBadge status={m.status} /></td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">{format(new Date(m.scheduledStart), 'MMM d, HH:mm')}</td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">{m.scheduledEnd ? format(new Date(m.scheduledEnd), 'MMM d, HH:mm') : '—'}</td>
                    <td className="px-4 py-3 text-gray-400">{m.reportedByUsername}</td>
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
