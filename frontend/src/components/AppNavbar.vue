<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppIcon from './AppIcon.vue'
import BrandMark from './BrandMark.vue'
import NotificationBell from './NotificationBell.vue'

const emit = defineEmits(['toggle-sidebar'])

const auth = useAuthStore()
const route = useRoute()

const title = computed(() => route.meta.title ?? '')

const today = new Intl.DateTimeFormat('en-GB', { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date())
</script>

<template>
  <header class="topbar">
    <div class="left">
      <button class="icon-btn menu-btn" aria-label="Open menu" @click="emit('toggle-sidebar')">
        <AppIcon name="menu" />
      </button>
      <BrandMark class="mobile-brand" :size="30" />
      <div class="heading">
        <p class="crumb">Workspace</p>
        <p class="current">{{ title }}</p>
      </div>
    </div>

    <div class="right">
      <span class="today">{{ today }}</span>
      <NotificationBell v-if="auth.isSuperAdmin" />
    </div>
  </header>
</template>

<style scoped>
.topbar {
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 28px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 30;
}
.left,
.right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.heading {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
}
.heading p {
  margin: 0;
}
.crumb {
  color: var(--text-muted);
}
.crumb::after {
  content: '/';
  margin-left: 8px;
  color: var(--border-strong);
}
.current {
  font-weight: 600;
  color: var(--text);
}
.today {
  font-size: 13px;
  color: var(--text-muted);
}
.menu-btn,
.mobile-brand {
  display: none;
}

@media (max-width: 900px) {
  .topbar {
    padding: 0 16px;
  }
  .menu-btn,
  .mobile-brand {
    display: inline-flex;
  }
  .crumb {
    display: none;
  }
}
@media (max-width: 560px) {
  .today {
    display: none;
  }
}
</style>
