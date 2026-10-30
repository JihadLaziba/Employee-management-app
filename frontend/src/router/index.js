import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import LoginView from '@/views/LoginView.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import EmployeesView from '@/views/EmployeesView.vue'
import UsersView from '@/views/UsersView.vue'
import ForbiddenView from '@/views/ForbiddenView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: { name: 'employees' } },
        {
          path: 'employees',
          name: 'employees',
          component: EmployeesView,
          meta: { title: 'Employees', permission: 'READ' },
        },
        {
          path: 'users',
          name: 'users',
          component: UsersView,
          meta: { title: 'Users', superAdminOnly: true },
        },
        {
          path: 'forbidden',
          name: 'forbidden',
          component: ForbiddenView,
          meta: { title: 'Access restricted' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.matched.some((r) => r.meta.requiresAuth)) {
    if (!auth.isAuthenticated) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (!auth.user) {
      try {
        await auth.fetchMe()
      } catch {
        auth.logout()
        return { name: 'login', query: { expired: '1' } }
      }
    }
    if (to.meta.superAdminOnly && !auth.isSuperAdmin) {
      return { name: 'forbidden' }
    }
    if (to.meta.permission && !auth.can(to.meta.permission)) {
      return { name: 'forbidden' }
    }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { path: '/' }
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} – EMS` : 'EMS – Employee Management'
})

export default router
