<script setup>
import { computed } from 'vue'
import AppIcon from './AppIcon.vue'

const props = defineProps({
  page: { type: Number, required: true }, 
  size: { type: Number, required: true },
  totalElements: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  disabled: { type: Boolean, default: false },
})
const emit = defineEmits(['change', 'size-change'])

const from = computed(() => (props.totalElements === 0 ? 0 : props.page * props.size + 1))
const to = computed(() => Math.min((props.page + 1) * props.size, props.totalElements))


const pages = computed(() => {
  const total = props.totalPages
  const current = props.page
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const set = new Set([0, total - 1, current - 1, current, current + 1])
  const list = [...set].filter((p) => p >= 0 && p < total).sort((a, b) => a - b)
  const result = []
  list.forEach((p, i) => {
    if (i > 0 && p - list[i - 1] > 1) result.push('…')
    result.push(p)
  })
  return result
})
</script>

<template>
  <div class="pagination">
    <span class="info">
      Showing <strong>{{ from }}–{{ to }}</strong> of <strong>{{ totalElements }}</strong>
    </span>

    <div class="controls">
      <label class="size">
        <span>Rows</span>
        <select
          class="select"
          :value="size"
          :disabled="disabled"
          @change="emit('size-change', Number($event.target.value))"
        >
          <option v-for="n in [5, 10, 20, 50]" :key="n" :value="n">{{ n }}</option>
        </select>
      </label>

      <button
        class="icon-btn"
        aria-label="Previous page"
        :disabled="disabled || page === 0"
        @click="emit('change', page - 1)"
      >
        <AppIcon name="chevronLeft" />
      </button>

      <template v-for="(p, i) in pages" :key="i">
        <span v-if="p === '…'" class="dots">…</span>
        <button
          v-else
          class="page-btn"
          :class="{ active: p === page }"
          :disabled="disabled"
          @click="p !== page && emit('change', p)"
        >
          {{ p + 1 }}
        </button>
      </template>

      <button
        class="icon-btn"
        aria-label="Next page"
        :disabled="disabled || page >= totalPages - 1"
        @click="emit('change', page + 1)"
      >
        <AppIcon name="chevronRight" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--border);
}
.info {
  color: var(--text-muted);
  font-size: 13px;
}
.info strong {
  color: var(--text);
}
.controls {
  display: flex;
  align-items: center;
  gap: 4px;
}
.size {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-right: 8px;
  font-size: 13px;
  color: var(--text-muted);
}
.size .select {
  width: 68px;
  height: 32px;
  padding: 0 8px;
}
.page-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border-radius: var(--radius);
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}
.page-btn:hover:not(:disabled):not(.active) {
  background: var(--surface-hover);
}
.page-btn.active {
  background: var(--primary);
  color: #fff;
}
.dots {
  padding: 0 4px;
  color: var(--text-muted);
}
</style>
