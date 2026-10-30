<script setup>
import AppIcon from '@/components/AppIcon.vue'
</script>

<template>
  <div class="card state">
    <span class="state-icon"><AppIcon name="lock" :size="28" /></span>
    <h2>Access restricted</h2>
    <p>You do not have permission to view this page. Please contact a super administrator.</p>
  </div>
</template>

<style scoped>
.state {
  max-width: 480px;
  margin: 40px auto;
  padding: 40px 24px;
  text-align: center;
}
.state-icon {
  display: inline-grid;
  place-items: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--warning-soft);
  color: var(--warning);
  margin-bottom: 12px;
}
h2 {
  margin: 0 0 6px;
  font-size: 18px;
}
p {
  margin: 0;
  color: var(--text-muted);
}
</style>
