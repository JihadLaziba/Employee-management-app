<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppNavbar from '@/components/AppNavbar.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import SessionTimeoutModal from '@/components/SessionTimeoutModal.vue'
import { useSessionTimeout } from '@/composables/useSessionTimeout'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const sidebarOpen = ref(false)
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

// Referme la sidebar mobile à chaque changement de page
watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  },
)

// Déconnexion automatique après inactivité
const session = useSessionTimeout({
  refresh: () => auth.refresh(),
  onTimeout: async () => {
    await auth.logout('idle')
    router.push({ name: 'login', query: { reason: 'idle' } })
  },
  onLogout: async () => {
    await auth.logout()
    toast.success('You have been signed out')
    router.push({ name: 'login' })
  },
})
</script>

<template>
  <div class="shell">
    <AppSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <div class="main">
      <AppNavbar @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <main class="content">
        <RouterView />
      </main>
    </div>

    <SessionTimeoutModal
      :open="session.warningVisible.value"
      :seconds-left="session.secondsLeft.value"
      :extending="session.extending.value"
      @stay="session.stayConnected"
      @logout="session.logoutNow"
    />
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
}
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.content {
  flex: 1;
  width: 100%;
  max-width: 1320px;
  padding: 28px 28px 40px;
}
@media (max-width: 560px) {
  .content {
    padding: 18px 16px 32px;
  }
}
</style>
