import { defineStore } from 'pinia'
import http from '@/api/http'

/**
 * État de connexion.
 * Le token n'est plus stocké côté JavaScript : il vit dans un cookie HttpOnly.
 * Pour savoir si l'utilisateur est connecté, on interroge simplement /auth/me.
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    /** true une fois qu'on a vérifié la session au démarrage */
    initialized: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.user,
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
    /**
     * Sert uniquement à adapter l'interface (cacher un bouton).
     * La vraie vérification est faite par le backend.
     */
    can(permission) {
      return this.isSuperAdmin || (this.user?.permissions ?? []).includes(permission)
    },

    /** Au chargement de l'application : y a-t-il déjà une session valide ? */
    async init() {
      if (this.initialized) return
      try {
        const { data } = await http.get('/auth/me', { skipAuthRedirect: true })
        this.user = data
      } catch {
        this.user = null
      } finally {
        this.initialized = true
      }
    },

    async login(email, password) {
      const { data } = await http.post('/auth/login', { email, password }, { skipAuthRedirect: true })
      this.user = data.user
      this.initialized = true
    },

    /** Recharge l'utilisateur et ses permissions à jour. */
    async fetchMe() {
      const { data } = await http.get('/auth/me')
      this.user = data
      return data
    },

    /** Prolonge la session (nouveau cookie avec une nouvelle date d'expiration). */
    async refresh() {
      const { data } = await http.post('/auth/refresh', null, { skipAuthRedirect: true })
      this.user = data.user
    },

    /**
     * Déconnexion : le backend supprime le cookie.
     * @param reason "idle" quand la déconnexion vient du minuteur d'inactivité
     */
    async logout(reason) {
      try {
        await http.post('/auth/logout', null, { params: reason ? { reason } : {}, skipAuthRedirect: true })
      } catch {
        /* même si l'appel échoue, on déconnecte localement */
      }
      this.clear()
    },

    /** Oublie l'utilisateur côté interface (sans appel au serveur). */
    clear() {
      this.user = null
    },
  },
})
