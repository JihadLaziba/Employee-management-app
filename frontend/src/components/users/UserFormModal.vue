<script setup>
import { computed, reactive, ref, watch } from 'vue'
import AppModal from '@/components/AppModal.vue'
import { usersApi } from '@/api/users'
import { getErrorMessage, getFieldErrors } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { EMAIL_PATTERN, PERMISSIONS, ROLES } from '@/utils/constants'

const props = defineProps({
  open: { type: Boolean, default: false },
  user: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const auth = useAuthStore()

const isEdit = computed(() => !!props.user)
const isSelf = computed(() => isEdit.value && props.user.id === auth.user?.id)

const form = reactive({ name: '', email: '', password: '', role: 'USER', enabled: true, permissions: ['READ'] })
const errors = reactive({})
const formError = ref('')
const saving = ref(false)

function reset() {
  Object.keys(errors).forEach((k) => delete errors[k])
  formError.value = ''
  if (props.user) {
    form.name = props.user.name
    form.email = props.user.email
    form.password = ''
    form.role = props.user.role
    form.enabled = props.user.enabled
    form.permissions = [...props.user.permissions]
  } else {
    Object.assign(form, { name: '', email: '', password: '', role: 'USER', enabled: true, permissions: ['READ'] })
  }
}

watch(
  () => props.open,
  (open) => open && reset(),
)

Object.keys(form).forEach((key) => {
  watch(
    () => form[key],
    () => {
      delete errors[key]
    },
  )
})

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k])
  if (!form.name.trim()) errors.name = 'Name is required'
  if (!form.email.trim()) errors.email = 'Email is required'
  else if (!EMAIL_PATTERN.test(form.email.trim())) errors.email = 'Email format is invalid'
  if (!isEdit.value && !form.password) errors.password = 'Password is required'
  else if (form.password && form.password.length < 8) errors.password = 'Password must be at least 8 characters'
  return Object.keys(errors).length === 0
}

async function submit() {
  formError.value = ''
  if (!validate()) return

  saving.value = true
  try {
    const base = {
      name: form.name.trim(),
      email: form.email.trim(),
      role: form.role,
      enabled: form.enabled,
    }
    let saved
    if (isEdit.value) {
      saved = await usersApi.update(props.user.id, { ...base, password: form.password || null })
      if (isSelf.value) await auth.fetchMe()
    } else {
      saved = await usersApi.create({
        ...base,
        password: form.password,
        permissions: form.role === 'USER' ? form.permissions : [],
      })
    }
    emit('saved', saved)
  } catch (e) {
    Object.assign(errors, getFieldErrors(e))
    formError.value = getErrorMessage(e, 'Unable to save the user.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AppModal :open="open" :title="isEdit ? 'Edit user' : 'Create user'" :busy="saving" @close="emit('close')">
    <div v-if="formError" class="alert alert-error" role="alert">{{ formError }}</div>

    <form id="user-form" novalidate @submit.prevent="submit">
      <div class="field">
        <label for="u-name" class="required">Name</label>
        <input id="u-name" v-model="form.name" class="input" :class="{ 'input-error': errors.name }" maxlength="100" />
        <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
      </div>

      <div class="field">
        <label for="u-email" class="required">Email</label>
        <input
          id="u-email"
          v-model="form.email"
          type="email"
          class="input"
          :class="{ 'input-error': errors.email }"
          maxlength="150"
          autocomplete="off"
        />
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </div>

      <div class="field">
        <label for="u-password" :class="{ required: !isEdit }">Password</label>
        <input
          id="u-password"
          v-model="form.password"
          type="password"
          class="input"
          :class="{ 'input-error': errors.password }"
          autocomplete="new-password"
          :placeholder="isEdit ? 'Leave empty to keep the current password' : 'At least 8 characters'"
        />
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
      </div>

      <div class="grid-2">
        <div class="field">
          <label for="u-role" class="required">User type</label>
          <select id="u-role" v-model="form.role" class="select" :disabled="isSelf">
            <option v-for="r in ROLES" :key="r.value" :value="r.value">{{ r.label }}</option>
          </select>
          <span v-if="isSelf" class="field-hint">You cannot change your own role.</span>
        </div>

        <div class="field">
          <label for="u-status">Status</label>
          <select id="u-status" v-model="form.enabled" class="select" :disabled="isSelf">
            <option :value="true">Active</option>
            <option :value="false">Disabled</option>
          </select>
          <span v-if="isSelf" class="field-hint">You cannot disable your own account.</span>
        </div>
      </div>

      <div v-if="!isEdit && form.role === 'USER'" class="field">
        <label>Permissions</label>
        <div class="perm-list">
          <label v-for="p in PERMISSIONS" :key="p.value" class="perm">
            <input v-model="form.permissions" type="checkbox" :value="p.value" />
            <span>
              <strong>{{ p.label }}</strong>
              <small>{{ p.description }}</small>
            </span>
          </label>
        </div>
      </div>
      <p v-else-if="form.role === 'SUPER_ADMIN'" class="field-hint">
        Super administrators automatically have all permissions.
      </p>
    </form>

    <template #footer>
      <button class="btn btn-secondary" :disabled="saving" @click="emit('close')">Cancel</button>
      <button type="submit" form="user-form" class="btn btn-primary" :disabled="saving">
        <span v-if="saving" class="spinner" />
        {{ isEdit ? 'Save changes' : 'Create user' }}
      </button>
    </template>
  </AppModal>
</template>
