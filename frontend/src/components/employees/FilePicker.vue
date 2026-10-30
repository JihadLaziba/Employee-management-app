<script setup>
import { ref } from 'vue'
import AppIcon from '@/components/AppIcon.vue'
import { formatFileSize } from '@/utils/files'

const props = defineProps({
  accept: { type: String, default: '' },
  hint: { type: String, default: '' },
  file: { type: Object, default: null },
  currentLabel: { type: String, default: '' },
  error: { type: String, default: '' },
  icon: { type: String, default: 'upload' },
  disabled: { type: Boolean, default: false },
})
const emit = defineEmits(['select', 'clear'])

const input = ref(null)
const dragging = ref(false)

function onChange(e) {
  const f = e.target.files?.[0]
  if (f) emit('select', f)
  e.target.value = '' 
}

function onDrop(e) {
  dragging.value = false
  if (props.disabled) return
  const f = e.dataTransfer.files?.[0]
  if (f) emit('select', f)
}
</script>

<template>
  <div>
    <div
      class="picker"
      :class="{ dragging, error, disabled }"
      role="button"
      tabindex="0"
      @click="!disabled && input.click()"
      @keydown.enter.prevent="!disabled && input.click()"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <AppIcon :name="file ? 'check' : icon" :size="18" class="picker-icon" />
      <div class="picker-text">
        <template v-if="file">
          <strong>{{ file.name }}</strong>
          <small>{{ formatFileSize(file.size) }} · click to change</small>
        </template>
        <template v-else-if="currentLabel">
          <strong>{{ currentLabel }}</strong>
          <small>Click or drop a file to replace · {{ hint }}</small>
        </template>
        <template v-else>
          <strong>Choose a file</strong>
          <small>or drop it here · {{ hint }}</small>
        </template>
      </div>
      <button
        v-if="file"
        type="button"
        class="icon-btn"
        aria-label="Remove selected file"
        @click.stop="emit('clear')"
      >
        <AppIcon name="x" :size="16" />
      </button>
      <input ref="input" type="file" :accept="accept" hidden @change="onChange" />
    </div>
    <span v-if="error" class="field-error">{{ error }}</span>
  </div>
</template>

<style scoped>
.picker {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1.5px dashed var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.picker:hover,
.picker.dragging {
  border-color: var(--primary);
  background: var(--primary-soft);
}
.picker.error {
  border-color: var(--danger);
}
.picker.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.picker-icon {
  color: var(--primary);
}
.picker-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.picker-text strong {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.picker-text small {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
