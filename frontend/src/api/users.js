import http from './http'

export const usersApi = {
  list({ search = '', page = 0, size = 10, sortBy = 'createdAt', direction = 'desc' } = {}) {
    return http.get('/users', { params: { search, page, size, sortBy, direction } }).then((r) => r.data)
  },
  get(id) {
    return http.get(`/users/${id}`).then((r) => r.data)
  },
  create(payload) {
    return http.post('/users', payload).then((r) => r.data)
  },
  update(id, payload) {
    return http.put(`/users/${id}`, payload).then((r) => r.data)
  },
  updatePermissions(id, permissions) {
    return http.put(`/users/${id}/permissions`, { permissions }).then((r) => r.data)
  },
  remove(id) {
    return http.delete(`/users/${id}`)
  },
}
