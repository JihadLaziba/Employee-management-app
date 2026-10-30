import { defineStore } from 'pinia'
import http, { tokenStorage } from '@/api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: tokenStorage.get(),
    user: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isSuperAdmin: (state) => state.user?.role === 'SUPER_ADMIN',
    initials: (state) =>
      (state.user?.name ?? '')
        .split(' ')
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join(''),
  },

  actions: {
    can(permission) {
      return this.isSuperAdmin || (this.user?.permissions ?? []).includes(permission)
    },

    async login(email, password) {
      const { data } = await http.post('/auth/login', { email, password }, { skipAuthRedirect: true })
      this.token = data.token
      this.user = data.user
      tokenStorage.set(data.token)
    },

    async fetchMe() {
      const { data } = await http.get('/auth/me')
      this.user = data
      return data
    },

    logout() {
      this.token = null
      this.user = null
      tokenStorage.clear()
    },
  },
})
