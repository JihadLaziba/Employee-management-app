<script setup>
import AppModal from '@/components/AppModal.vue'
import { formatDateTime, initials } from '@/utils/format'

defineProps({
  open: { type: Boolean, default: false },
  user: { type: Object, default: null },
})
const emit = defineEmits(['close'])
</script>

<template>
  <AppModal :open="open" title="User details" width="480px" @close="emit('close')">
    <template v-if="user">
      <div class="head">
        <span class="avatar-lg">{{ initials(user.name) }}</span>
        <div>
          <h3>{{ user.name }}</h3>
          <p>{{ user.email }}</p>
        </div>
      </div>

      <dl class="details">
        <dt>User type</dt>
        <dd>
          <span class="badge" :class="user.role === 'SUPER_ADMIN' ? 'badge-primary' : ''">
            {{ user.role === 'SUPER_ADMIN' ? 'Super Administrator' : 'Normal User' }}
          </span>
        </dd>

        <dt>Status</dt>
        <dd>
          <span class="badge" :class="user.enabled ? 'badge-success' : 'badge-danger'">
            {{ user.enabled ? 'Active' : 'Disabled' }}
          </span>
        </dd>

        <dt>Permissions</dt>
        <dd class="perms">
          <span v-if="user.role === 'SUPER_ADMIN'" class="badge badge-primary">All permissions</span>
          <template v-else-if="user.permissions.length">
            <span v-for="p in user.permissions" :key="p" class="badge">{{ p }}</span>
          </template>
          <span v-else class="muted">No permissions</span>
        </dd>

        <dt>Created</dt>
        <dd>{{ formatDateTime(user.createdAt) }}</dd>

        <dt>Last updated</dt>
        <dd>{{ formatDateTime(user.updatedAt) }}</dd>
      </dl>
    </template>

    <template #footer>
      <button class="btn btn-secondary" @click="emit('close')">Close</button>
    </template>
  </AppModal>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}
.head h3 {
  margin: 0;
  font-size: 17px;
}
.head p {
  margin: 2px 0 0;
  color: var(--text-muted);
}
.avatar-lg {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--primary-soft);
  color: var(--primary);
  font-weight: 700;
  font-size: 18px;
}
.perms {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.muted {
  color: var(--text-muted);
}
</style>
