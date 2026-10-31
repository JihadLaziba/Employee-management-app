<script setup>
import { computed } from 'vue'
import AppIcon from './AppIcon.vue'
import { WARNING_DURATION_MS } from '@/composables/useSessionTimeout'

const props = defineProps({
  open: { type: Boolean, default: false },
  secondsLeft: { type: Number, default: 60 },
  extending: { type: Boolean, default: false },
})
const emit = defineEmits(['stay', 'logout'])

const total = WARNING_DURATION_MS / 1000
/** Progression du cercle du compte à rebours (0 à 1) */
const progress = computed(() => Math.max(0, Math.min(1, props.secondsLeft / total)))
const CIRCUMFERENCE = 2 * Math.PI * 26
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="open" class="overlay">
        <div class="dialog" role="alertdialog" aria-modal="true" aria-labelledby="session-title">
          <div class="countdown" aria-hidden="true">
            <svg viewBox="0 0 60 60" width="64" height="64">
              <circle cx="30" cy="30" r="26" class="track" />
              <circle
                cx="30"
                cy="30"
                r="26"
                class="bar"
                :stroke-dasharray="CIRCUMFERENCE"
                :stroke-dashoffset="CIRCUMFERENCE * (1 - progress)"
              />
            </svg>
            <span class="seconds">{{ secondsLeft }}</span>
          </div>

          <h2 id="session-title">Are you still there?</h2>
          <p>
            You have been inactive for a while. For your security, you will be signed out in
            <strong>{{ secondsLeft }} second{{ secondsLeft === 1 ? '' : 's' }}</strong>.
          </p>

          <div class="actions">
            <button class="btn btn-secondary" :disabled="extending" @click="emit('logout')">
              <AppIcon name="logout" :size="16" />
              Sign out
            </button>
            <button class="btn btn-primary" :disabled="extending" autofocus @click="emit('stay')">
              <span v-if="extending" class="spinner" />
              Stay signed in
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(2px);
  display: grid;
  place-items: center;
  padding: 16px;
}
.dialog {
  width: 100%;
  max-width: 400px;
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 28px 24px 22px;
  text-align: center;
}
.countdown {
  position: relative;
  width: 64px;
  height: 64px;
  margin: 0 auto 14px;
}
.countdown svg {
  transform: rotate(-90deg);
}
.track {
  fill: none;
  stroke: var(--border);
  stroke-width: 5;
}
.bar {
  fill: none;
  stroke: var(--warning);
  stroke-width: 5;
  stroke-linecap: round;
  transition: stroke-dashoffset 1s linear;
}
.seconds {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
h2 {
  margin: 0 0 8px;
  font-size: 18px;
}
p {
  margin: 0 0 22px;
  color: var(--text-secondary);
}
.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.actions .btn {
  flex: 1;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
