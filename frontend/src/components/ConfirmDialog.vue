<script setup>
import AppModal from './AppModal.vue'
import AppIcon from './AppIcon.vue'

defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: 'Are you sure?' },
  message: { type: String, default: '' },
  confirmLabel: { type: String, default: 'Delete' },
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <AppModal :open="open" :title="title" width="440px" :busy="loading" @close="emit('cancel')">
    <div class="confirm">
      <span class="confirm-icon"><AppIcon name="alert" :size="22" /></span>
      <p>{{ message }}</p>
    </div>
    <template #footer>
      <button class="btn btn-secondary" :disabled="loading" @click="emit('cancel')">Cancel</button>
      <button class="btn btn-danger" :disabled="loading" @click="emit('confirm')">
        <span v-if="loading" class="spinner" />
        {{ confirmLabel }}
      </button>
    </template>
  </AppModal>
</template>

<style scoped>
.confirm {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}
.confirm-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--danger-soft);
  color: var(--danger);
}
.confirm p {
  margin: 8px 0 0;
  color: var(--text-secondary);
}
</style>
