import axios from 'axios'

/**
 * Client HTTP de l'application.
 *
 * Authentification : le JWT est dans un cookie HttpOnly posé par le backend.
 * Le navigateur l'envoie automatiquement, le JavaScript n'y a jamais accès.
 *
 * CSRF : le backend pose un cookie lisible "XSRF-TOKEN" ; axios le renvoie
 * automatiquement dans l'en-tête "X-XSRF-TOKEN" (comportement par défaut d'axios
 * pour les requêtes vers la même origine).
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

// 401 = session expirée -> déconnexion (sauf pour les requêtes qui le demandent)
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.skipAuthRedirect && unauthorizedHandler) {
      unauthorizedHandler()
    }
    return Promise.reject(error)
  },
)

/**
 * Message lisible pour l'utilisateur.
 * Le backend renvoie toujours { message: "..." } : on n'affiche jamais d'erreur technique.
 */
export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (!error?.response) {
    return 'Unable to reach the server. Please check your connection.'
  }
  const message = error.response.data?.message
  return typeof message === 'string' && message ? message : fallback
}

/** Erreurs de validation par champ : { email: "Email is required", ... } */
export function getFieldErrors(error) {
  return error?.response?.data?.fieldErrors ?? {}
}

export default http
