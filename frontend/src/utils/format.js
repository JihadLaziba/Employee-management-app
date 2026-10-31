const dateFormatter = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
const dateTimeFormatter = new Intl.DateTimeFormat('en-GB', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

/** "2025-03-01" -> "01 Mar 2025" (sans décalage de fuseau horaire) */
export function formatDate(value) {
  if (!value) return '—'
  const [y, m, d] = String(value).slice(0, 10).split('-').map(Number)
  return dateFormatter.format(new Date(y, m - 1, d))
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : dateTimeFormatter.format(date)
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '—'
  return (
    new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(Number(value)) +
    ' MAD'
  )
}

export function initials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')
}

const relative = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

/** "2025-03-01T10:00:00" -> "5 minutes ago" */
export function timeAgo(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const seconds = Math.round((date.getTime() - Date.now()) / 1000)
  const abs = Math.abs(seconds)
  if (abs < 45) return 'just now'
  if (abs < 3600) return relative.format(Math.round(seconds / 60), 'minute')
  if (abs < 86400) return relative.format(Math.round(seconds / 3600), 'hour')
  if (abs < 86400 * 30) return relative.format(Math.round(seconds / 86400), 'day')
  return formatDate(value)
}

const timeFormatter = new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit' })
const dayMonthFormatter = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short' })

/** "Today at 11:49", "Yesterday at 09:12", "28 Oct at 14:03", ou date complète si autre année */
export function formatWhen(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
  const days = Math.round((startOfDay(now) - startOfDay(date)) / 86400000)
  const time = timeFormatter.format(date)
  if (days === 0) return `Today at ${time}`
  if (days === 1) return `Yesterday at ${time}`
  if (date.getFullYear() === now.getFullYear()) return `${dayMonthFormatter.format(date)} at ${time}`
  return formatDateTime(value)
}
