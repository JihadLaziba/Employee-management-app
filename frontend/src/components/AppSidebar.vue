<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import AppIcon from './AppIcon.vue'
import BrandMark from './BrandMark.vue'

defineProps({
  open: { type: Boolean, default: false },
})
const emit = defineEmits(['close'])

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()

/** Les deux menus exigés. Un menu inaccessible reste visible avec un cadenas. */
const items = computed(() => [
  {
    to: { name: 'employees' },
    label: 'Employees',
    hint: 'Records and contracts',
    icon: 'briefcase',
    locked: !auth.can('READ'),
  },
  {
    to: { name: 'users' },
    label: 'Users',
    hint: 'Access and permissions',
    icon: 'users',
    locked: !auth.isSuperAdmin,
  },
])

async function signOut() {
  await auth.logout()
  toast.success('You have been signed out')
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="backdrop" :class="{ visible: open }" @click="emit('close')" />
  <aside class="sidebar" :class="{ open }" aria-label="Main navigation">
    <RouterLink to="/" class="brand" @click="emit('close')">
      <BrandMark :size="34" />
      <span class="brand-text">
        <strong>EMS</strong>
        <span>People office</span>
      </span>
    </RouterLink>

    <nav class="nav">
      <RouterLink
        v-for="item in items"
        :key="item.label"
        :to="item.to"
        class="nav-link"
        :class="{ locked: item.locked }"
        @click="emit('close')"
      >
        <AppIcon :name="item.icon" :size="19" />
        <span class="nav-text">
          <span class="nav-label">{{ item.label }}</span>
          <span class="nav-hint">{{ item.locked ? 'No access' : item.hint }}</span>
        </span>
        <AppIcon v-if="item.locked" name="lock" :size="14" class="lock" />
      </RouterLink>
    </nav>

    <div class="account">
      <span class="avatar">{{ auth.initials }}</span>
      <span class="account-text">
        <span class="account-name">{{ auth.user?.name }}</span>
        <span class="account-role">{{ auth.isSuperAdmin ? 'Super administrator' : 'User' }}</span>
      </span>
      <button class="signout" title="Sign out" aria-label="Sign out" @click="signOut">
        <AppIcon name="logout" :size="17" />
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  position: sticky;
  top: 0;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--ink);
  color: var(--ink-text);
  padding: 18px 14px 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 4px 8px 22px;
  color: #fff;
  text-decoration: none;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.brand-text strong {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.brand-text span {
  font-size: 12.5px;
  color: var(--ink-text);
  opacity: 0.8;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius);
  color: var(--ink-text);
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: var(--ink-soft);
  color: #fff;
}
.nav-link.router-link-active {
  background: var(--ink-soft);
  color: #fff;
}
/* Repère laiton à gauche de la page active */
.nav-link.router-link-active::before {
  content: '';
  position: absolute;
  left: -14px;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--brass);
}
.nav-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  min-width: 0;
}
.nav-label {
  font-size: 14.5px;
  font-weight: 500;
}
.nav-hint {
  font-size: 12px;
  opacity: 0.65;
}
.nav-link.locked {
  opacity: 0.5;
}
.lock {
  margin-left: auto;
}

.account {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 8px 4px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--brass-soft);
  color: #6b4a14;
  font-size: 12.5px;
  font-weight: 700;
  flex-shrink: 0;
}
.account-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  line-height: 1.25;
}
.account-name {
  color: #fff;
  font-size: 13.5px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.account-role {
  font-size: 12px;
  opacity: 0.7;
}
.signout {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--ink-text);
  display: grid;
  place-items: center;
  cursor: pointer;
}
.signout:hover {
  background: var(--ink-soft);
  color: #fff;
}
.signout:focus-visible {
  outline-color: var(--brass);
}

.backdrop {
  display: none;
}

/* Mobile : panneau coulissant */
@media (max-width: 900px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 50;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
    box-shadow: var(--shadow-lg);
  }
  .sidebar.open {
    transform: translateX(0);
  }
  .backdrop {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(23, 32, 51, 0.45);
    z-index: 45;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s;
  }
  .backdrop.visible {
    opacity: 1;
    pointer-events: auto;
  }
}
</style>
