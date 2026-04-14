import { useQuery } from '@tanstack/react-query'
import api from '../lib/api'
import type { DailyNote } from '../types'
import { PageLoader } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { PageHeader } from '../components/PageHeader'
import { EmptyState } from '../components/EmptyState'
import { format } from 'date-fns'
import { Pin } from 'lucide-react'

export default function DailyNotesPage() {
  const today = format(new Date(), 'yyyy-MM-dd')
  const { data, isLoading } = useQuery<DailyNote[]>({
    queryKey: ['notes', today],
    queryFn: () => api.get('/api/notes', { params: { date: today } }).then(r => r.data),
  })

  if (isLoading) return <PageLoader />

  return (
    <div>
      <PageHeader title="Daily Notes" subtitle={`Shift notes for ${format(new Date(), 'MMMM d, yyyy')}`} />
      <div className="px-8">
        {!data?.length ? (
          <EmptyState title="No notes today" description="No shift notes have been added for today." />
        ) : (
          <div className="space-y-4">
            {data.map(note => (
              <div key={note.id} className="card p-5">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      {note.pinned && <Pin size={14} className="text-orange-500" />}
                      <h3 className="font-semibold text-gray-900">{note.title}</h3>
                      <StatusBadge status={note.importance} />
                      <span className="badge bg-gray-100 text-gray-600">{note.category}</span>
                    </div>
                    <p className="text-gray-600 text-sm leading-relaxed mt-2">{note.content}</p>
                  </div>
                </div>
                <div className="mt-3 pt-3 border-t border-gray-100 flex gap-4 text-xs text-gray-400">
                  <span>By {note.authorUsername}</span>
                  <span>{format(new Date(note.createdAt), 'HH:mm')}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
