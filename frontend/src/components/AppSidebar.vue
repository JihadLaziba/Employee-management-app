<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppIcon from './AppIcon.vue'

defineProps({
  open: { type: Boolean, default: false },
})
const emit = defineEmits(['close'])

const auth = useAuthStore()

const items = computed(() => [
  { to: { name: 'users' }, label: 'Users', icon: 'users', locked: !auth.isSuperAdmin },
  { to: { name: 'employees' }, label: 'Employees', icon: 'briefcase', locked: !auth.can('READ') },
])
</script>

<template>
  <div class="backdrop" :class="{ visible: open }" @click="emit('close')" />
  <aside class="sidebar" :class="{ open }">
    <p class="sidebar-title">Menu</p>
    <nav>
      <RouterLink
        v-for="item in items"
        :key="item.label"
        :to="item.to"
        class="nav-link"
        :class="{ locked: item.locked }"
        @click="emit('close')"
      >
        <AppIcon :name="item.icon" />
        <span>{{ item.label }}</span>
        <AppIcon v-if="item.locked" name="lock" :size="14" class="lock" />
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background: var(--surface);
  border-right: 1px solid var(--border);
  padding: 20px 12px;
  height: calc(100vh - var(--navbar-height));
  position: sticky;
  top: var(--navbar-height);
  flex-shrink: 0;
}
.sidebar-title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  margin: 0 12px 10px;
}
nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius);
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: var(--surface-hover);
  color: var(--text);
}
.nav-link.router-link-active {
  background: var(--primary-soft);
  color: var(--primary);
  font-weight: 600;
}
.nav-link.locked {
  opacity: 0.6;
}
.lock {
  margin-left: auto;
}
.backdrop {
  display: none;
}

@media (max-width: 860px) {
  .sidebar {
    position: fixed;
    top: var(--navbar-height);
    left: 0;
    z-index: 40;
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
    inset: var(--navbar-height) 0 0 0;
    background: rgba(15, 23, 42, 0.35);
    z-index: 35;
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
