<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { employeesApi } from '@/api/employees'
import { initials } from '@/utils/format'


const props = defineProps({
  url: { type: String, default: null },
  name: { type: String, default: '' },
  size: { type: Number, default: 36 },
  version: { type: String, default: '' },
})

const objectUrl = ref(null)
const failed = ref(false)

function release() {
  if (objectUrl.value) {
    URL.revokeObjectURL(objectUrl.value)
    objectUrl.value = null
  }
}

async function load() {
  release()
  failed.value = false
  if (!props.url) return
  try {
    const { data } = await employeesApi.fetchBlob(props.url)
    objectUrl.value = URL.createObjectURL(data)
  } catch {
    failed.value = true
  }
}

watch(() => [props.url, props.version], load, { immediate: true })
onBeforeUnmount(release)
</script>

<template>
  <span class="photo" :style="{ width: `${size}px`, height: `${size}px`, fontSize: `${Math.round(size / 2.8)}px` }">
    <img v-if="objectUrl && !failed" :src="objectUrl" :alt="name" />
    <span v-else>{{ initials(name) }}</span>
  </span>
</template>

<style scoped>
.photo {
  border-radius: 50%;
  overflow: hidden;
  display: inline-grid;
  place-items: center;
  background: var(--primary-soft);
  color: var(--primary);
  font-weight: 700;
  flex-shrink: 0;
}
.photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
