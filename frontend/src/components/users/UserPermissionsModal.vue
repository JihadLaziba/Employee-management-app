<script setup>
import { ref, watch } from 'vue'
import AppModal from '@/components/AppModal.vue'
import { usersApi } from '@/api/users'
import { getErrorMessage } from '@/api/http'
import { PERMISSIONS } from '@/utils/constants'

const props = defineProps({
  open: { type: Boolean, default: false },
  user: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const selected = ref([])
const saving = ref(false)
const formError = ref('')

watch(
  () => props.open,
  (open) => {
    if (open && props.user) {
      selected.value = [...props.user.permissions]
      formError.value = ''
    }
  },
)

async function save() {
  saving.value = true
  formError.value = ''
  try {
    const updated = await usersApi.updatePermissions(props.user.id, selected.value)
    emit('saved', updated)
  } catch (e) {
    formError.value = getErrorMessage(e, 'Unable to update permissions.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AppModal :open="open" title="Manage permissions" width="480px" :busy="saving" @close="emit('close')">
    <p v-if="user" class="intro">
      Grant or revoke permissions for <strong>{{ user.name }}</strong>. Changes apply immediately, even if the user is
      already logged in.
    </p>

    <div v-if="formError" class="alert alert-error" role="alert">{{ formError }}</div>

    <div class="perm-list">
      <label v-for="p in PERMISSIONS" :key="p.value" class="perm">
        <input v-model="selected" type="checkbox" :value="p.value" />
        <span>
          <strong>{{ p.label }}</strong>
          <small>{{ p.description }}</small>
        </span>
      </label>
    </div>

    <template #footer>
      <button class="btn btn-secondary" :disabled="saving" @click="emit('close')">Cancel</button>
      <button class="btn btn-primary" :disabled="saving" @click="save">
        <span v-if="saving" class="spinner" />
        Save permissions
      </button>
    </template>
  </AppModal>
</template>

<style scoped>
.intro {
  margin: 0 0 16px;
  color: var(--text-secondary);
}
</style>
