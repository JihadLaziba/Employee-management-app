import http from './http'

/** Le backend renvoie des URLs "/api/...", alors que axios a déjà baseURL = "/api". */
const toApiPath = (url) => url.replace(/^\/api/, '')

export const employeesApi = {
  list({ search = '', page = 0, size = 10, sortBy = 'createdAt', direction = 'desc' } = {}) {
    return http.get('/employees', { params: { search, page, size, sortBy, direction } }).then((r) => r.data)
  },
  get(id) {
    return http.get(`/employees/${id}`).then((r) => r.data)
  },
  create(payload) {
    return http.post('/employees', payload).then((r) => r.data)
  },
  update(id, payload) {
    return http.put(`/employees/${id}`, payload).then((r) => r.data)
  },
  remove(id) {
    return http.delete(`/employees/${id}`)
  },

  uploadPhoto(id, file) {
    const form = new FormData()
    form.append('file', file)
    return http.post(`/employees/${id}/photo`, form).then((r) => r.data)
  },
  uploadCv(id, file) {
    const form = new FormData()
    form.append('file', file)
    return http.post(`/employees/${id}/cv`, form).then((r) => r.data)
  },

  /** Télécharge un fichier protégé (photo, CV, contrat) en envoyant le JWT. */
  fetchBlob(url) {
    return http.get(toApiPath(url), { responseType: 'blob' })
  },
  contract(id) {
    return http.get(`/employees/${id}/contract`, { responseType: 'blob' })
  },
}
