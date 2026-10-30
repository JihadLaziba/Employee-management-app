<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import AppIcon from './AppIcon.vue'

const emit = defineEmits(['toggle-sidebar'])

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()

function logout() {
  auth.logout()
  toast.success('You have been logged out')
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="navbar">
    <div class="navbar-left">
      <button class="icon-btn menu-btn" aria-label="Toggle menu" @click="emit('toggle-sidebar')">
        <AppIcon name="menu" />
      </button>
      <RouterLink to="/" class="brand">
        <span class="brand-logo">
          <svg viewBox="0 0 32 32" width="30" height="30" aria-hidden="true">
            <rect width="32" height="32" rx="8" fill="currentColor" />
            <path d="M10 10h12M10 16h9M10 22h12" stroke="#fff" stroke-width="3" stroke-linecap="round" />
          </svg>
        </span>
        <span class="brand-text">
          <strong>EMS</strong>
          <small>Employee Management</small>
        </span>
      </RouterLink>
    </div>

    <div class="navbar-right">
      <div class="user">
        <span class="avatar">{{ auth.initials }}</span>
        <span class="user-info">
          <span class="user-name">{{ auth.user?.name }}</span>
          <span class="user-role">{{ auth.isSuperAdmin ? 'Super Administrator' : 'User' }}</span>
        </span>
      </div>
      <button class="btn btn-ghost logout-btn" @click="logout">
        <AppIcon name="logout" :size="16" />
        <span class="logout-label">Logout</span>
      </button>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 30;
}
.navbar-left,
.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text);
  text-decoration: none;
}
.brand-logo {
  color: var(--primary);
  display: flex;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.brand-text strong {
  font-size: 16px;
  letter-spacing: 0.04em;
}
.brand-text small {
  font-size: 11px;
  color: var(--text-muted);
}
.menu-btn {
  display: none;
}
.user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
}
.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
}
.user-role {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 860px) {
  .menu-btn {
    display: inline-flex;
  }
}
@media (max-width: 560px) {
  .brand-text small,
  .user-info,
  .logout-label {
    display: none;
  }
}
</style>
