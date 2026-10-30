<script setup>
import AppIcon from './AppIcon.vue'

const model = defineModel({ type: String, default: '' })
defineProps({
  placeholder: { type: String, default: 'Search…' },
})
</script>

<template>
  <div class="search">
    <AppIcon name="search" :size="16" class="search-icon" />
    <input v-model="model" type="search" class="input" :placeholder="placeholder" :aria-label="placeholder" />
    <button v-if="model" class="clear" aria-label="Clear search" @click="model = ''">
      <AppIcon name="x" :size="14" />
    </button>
  </div>
</template>

<style scoped>
.search {
  position: relative;
  width: 100%;
  max-width: 340px;
}
.search .input {
  padding-left: 36px;
  padding-right: 32px;
}
.search .input::-webkit-search-cancel-button {
  display: none;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}
.clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  padding: 4px;
}
</style>
