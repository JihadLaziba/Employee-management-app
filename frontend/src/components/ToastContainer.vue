<script setup>
import { useToastStore } from '@/stores/toast'
import AppIcon from './AppIcon.vue'

const toast = useToastStore()
</script>

<template>
  <div class="toasts" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="t in toast.items" :key="t.id" class="toast" :class="`toast-${t.type}`" role="status">
        <AppIcon :name="t.type === 'success' ? 'check' : 'alert'" :size="18" />
        <span class="toast-message">{{ t.message }}</span>
        <button class="toast-close" aria-label="Close" @click="toast.dismiss(t.id)">
          <AppIcon name="x" :size="16" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toasts {
  position: fixed;
  right: 16px;
  bottom: 16px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: min(380px, calc(100vw - 32px));
}
.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  border-left-width: 4px;
  box-shadow: var(--shadow-lg);
  font-size: 14px;
}
.toast-success {
  border-left-color: var(--success);
  color: var(--success);
}
.toast-error {
  border-left-color: var(--danger);
  color: var(--danger);
}
.toast-message {
  flex: 1;
  color: var(--text);
  line-height: 1.4;
}
.toast-close {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0;
  display: flex;
}
.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
