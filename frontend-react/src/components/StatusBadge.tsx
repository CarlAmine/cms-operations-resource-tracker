import clsx from 'clsx'

const statusConfig: Record<string, { label: string; className: string }> = {
  AVAILABLE:   { label: 'Available',    className: 'bg-green-100 text-green-800' },
  BOOKED:      { label: 'Booked',       className: 'bg-blue-100 text-blue-800' },
  MAINTENANCE: { label: 'Maintenance',  className: 'bg-yellow-100 text-yellow-800' },
  UNAVAILABLE: { label: 'Unavailable',  className: 'bg-red-100 text-red-800' },
  PENDING:     { label: 'Pending',      className: 'bg-gray-100 text-gray-800' },
  APPROVED:    { label: 'Approved',     className: 'bg-green-100 text-green-800' },
  CANCELLED:   { label: 'Cancelled',    className: 'bg-red-100 text-red-800' },
  COMPLETED:   { label: 'Completed',    className: 'bg-purple-100 text-purple-800' },
  PLANNED:     { label: 'Planned',      className: 'bg-blue-100 text-blue-800' },
  IN_PROGRESS: { label: 'In Progress',  className: 'bg-orange-100 text-orange-800' },
  LOW:         { label: 'Low',          className: 'bg-gray-100 text-gray-600' },
  NORMAL:      { label: 'Normal',       className: 'bg-blue-100 text-blue-700' },
  HIGH:        { label: 'High',         className: 'bg-orange-100 text-orange-700' },
  CRITICAL:    { label: 'Critical',     className: 'bg-red-100 text-red-700' },
}

export function StatusBadge({ status }: { status: string }) {
  const config = statusConfig[status] ?? { label: status, className: 'bg-gray-100 text-gray-700' }
  return (
    <span className={clsx('badge', config.className)}>
      {config.label}
    </span>
  )
}
