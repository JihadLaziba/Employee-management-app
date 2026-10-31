import http from './http'

export const notificationsApi = {
  list({ page = 0, size = 15 } = {}) {
    return http.get('/notifications', { params: { page, size } }).then((r) => r.data)
  },
  unreadCount() {
    return http.get('/notifications/unread-count').then((r) => r.data.count)
  },
  markAllRead() {
    return http.post('/notifications/mark-read')
  },
}
