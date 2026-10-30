<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from '@/components/AppNavbar.vue'
import AppSidebar from '@/components/AppSidebar.vue'

const sidebarOpen = ref(false)
const route = useRoute()

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  },
)
</script>

<template>
  <div class="shell">
    <AppNavbar @toggle-sidebar="sidebarOpen = !sidebarOpen" />
    <div class="body">
      <AppSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.body {
  display: flex;
  flex: 1;
}
.content {
  flex: 1;
  min-width: 0;
  padding: 28px;
}
@media (max-width: 560px) {
  .content {
    padding: 16px;
  }
}
</style>
