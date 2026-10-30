<script setup>
import AppIcon from './AppIcon.vue'

defineProps({
  loading: { type: Boolean, default: false },
  loaded: { type: Boolean, default: false },
  error: { type: String, default: '' },
  empty: { type: Boolean, default: false },
  emptyTitle: { type: String, default: 'No data' },
  emptyText: { type: String, default: '' },
  columns: { type: Number, default: 5 },
})
const emit = defineEmits(['retry'])
</script>

<template>
  <div v-if="error" class="state state-error">
    <span class="state-icon"><AppIcon name="alert" :size="24" /></span>
    <h3>Something went wrong</h3>
    <p>{{ error }}</p>
    <button class="btn btn-secondary" @click="emit('retry')">
      <AppIcon name="refresh" :size="16" />
      Try again
    </button>
  </div>

  <div v-else-if="loading && !loaded" class="skeleton" aria-busy="true" aria-label="Loading">
    <div v-for="row in 5" :key="row" class="skeleton-row">
      <span v-for="col in columns" :key="col" class="skeleton-cell" />
    </div>
  </div>

  <div v-else-if="empty" class="state">
    <span class="state-icon state-icon-muted"><AppIcon name="inbox" :size="24" /></span>
    <h3>{{ emptyTitle }}</h3>
    <p v-if="emptyText">{{ emptyText }}</p>
    <slot name="empty-action" />
  </div>

  <div v-else class="content" :class="{ refreshing: loading }">
    <slot />
  </div>
</template>

<style scoped>
.state {
  padding: 48px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.state h3 {
  margin: 0 0 4px;
  font-size: 16px;
}
.state p {
  margin: 0 0 16px;
  color: var(--text-muted);
  max-width: 420px;
}
.state-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  margin-bottom: 12px;
  background: var(--danger-soft);
  color: var(--danger);
}
.state-icon-muted {
  background: var(--surface-hover);
  color: var(--text-muted);
}
.skeleton {
  padding: 8px 16px;
}
.skeleton-row {
  display: flex;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}
.skeleton-cell {
  flex: 1;
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--surface-hover) 25%, var(--border) 50%, var(--surface-hover) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}
.content {
  transition: opacity 0.15s;
}
.content.refreshing {
  opacity: 0.55;
  pointer-events: none;
}
</style>
