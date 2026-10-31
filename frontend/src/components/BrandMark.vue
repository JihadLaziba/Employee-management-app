<script setup>
/** Logo de l'application : une fiche du personnel posée sur une autre. */
defineProps({
  size: { type: Number, default: 32 },
})
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 32 32" aria-hidden="true" class="brand-mark">
    <rect width="32" height="32" rx="8" class="bg" />
    <rect x="9.5" y="7" width="15" height="11" rx="2" class="back" />
    <rect x="7.5" y="10" width="15" height="11" rx="2" class="front" />
    <circle cx="11.5" cy="14.5" r="1.8" class="dot" />
    <path d="M15 13.5h5M15 16.5h3.5" class="line" />
    <path d="M7.5 25h17" class="base" />
  </svg>
</template>

<style scoped>
.brand-mark {
  flex-shrink: 0;
  display: block;
}
.bg {
  fill: var(--ink);
}
.back {
  fill: none;
  stroke: var(--brass);
  stroke-width: 1.6;
  opacity: 0.7;
}
.front {
  fill: var(--ink);
  stroke: #fff;
  stroke-width: 1.6;
}
.dot {
  fill: var(--brass);
}
.line,
.base {
  stroke: #fff;
  stroke-width: 1.6;
  stroke-linecap: round;
}
.base {
  opacity: 0.35;
}
</style>
