<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import AppModal from '@/components/AppModal.vue'
import EmployeePhoto from './EmployeePhoto.vue'
import FilePicker from './FilePicker.vue'
import { employeesApi } from '@/api/employees'
import { getErrorMessage, getFieldErrors } from '@/api/http'
import { EMAIL_PATTERN } from '@/utils/constants'
import { CV_RULES, PHOTO_RULES, validateCv, validatePhoto } from '@/utils/files'

const props = defineProps({
  open: { type: Boolean, default: false },
  employee: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const PHONE_PATTERN = /^[+0-9 ()-]{6,30}$/

const isEdit = computed(() => !!props.employee)

const empty = () => ({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  jobTitle: '',
  department: '',
  hireDate: '',
  salary: '',
})
const form = reactive(empty())
const errors = reactive({})
const formError = ref('')
const saving = ref(false)

const photoFile = ref(null)
const cvFile = ref(null)
const photoPreview = ref(null)

function setPhotoPreview(file) {
  if (photoPreview.value) URL.revokeObjectURL(photoPreview.value)
  photoPreview.value = file ? URL.createObjectURL(file) : null
}

function reset() {
  Object.keys(errors).forEach((k) => delete errors[k])
  formError.value = ''
  photoFile.value = null
  cvFile.value = null
  setPhotoPreview(null)
  const e = props.employee
  Object.assign(
    form,
    e
      ? {
          firstName: e.firstName,
          lastName: e.lastName,
          email: e.email,
          phone: e.phone ?? '',
          jobTitle: e.jobTitle,
          department: e.department ?? '',
          hireDate: e.hireDate,
          salary: e.salary ?? '',
        }
      : empty(),
  )
}

watch(
  () => props.open,
  (open) => open && reset(),
)
onBeforeUnmount(() => setPhotoPreview(null))

Object.keys(form).forEach((key) => {
  watch(
    () => form[key],
    () => {
      delete errors[key]
    },
  )
})

function selectPhoto(file) {
  errors.photo = validatePhoto(file)
  if (errors.photo) return
  photoFile.value = file
  setPhotoPreview(file)
}

function selectCv(file) {
  errors.cv = validateCv(file)
  if (errors.cv) return
  cvFile.value = file
}

function validate() {
  const keep = { photo: errors.photo, cv: errors.cv }
  Object.keys(errors).forEach((k) => delete errors[k])
  Object.entries(keep).forEach(([k, v]) => v && (errors[k] = v))

  if (!form.firstName.trim()) errors.firstName = 'First name is required'
  if (!form.lastName.trim()) errors.lastName = 'Last name is required'
  if (!form.email.trim()) errors.email = 'Email is required'
  else if (!EMAIL_PATTERN.test(form.email.trim())) errors.email = 'Email format is invalid'
  if (form.phone.trim() && !PHONE_PATTERN.test(form.phone.trim())) errors.phone = 'Phone number format is invalid'
  if (!form.jobTitle.trim()) errors.jobTitle = 'Job title is required'
  if (!form.hireDate) errors.hireDate = 'Hire date is required'
  if (form.salary !== '' && (Number.isNaN(Number(form.salary)) || Number(form.salary) < 0)) {
    errors.salary = 'Salary must be a positive number'
  }
  return Object.keys(errors).length === 0
}

async function submit() {
  formError.value = ''
  if (!validate()) return

  saving.value = true
  const payload = {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    email: form.email.trim(),
    phone: form.phone.trim() || null,
    jobTitle: form.jobTitle.trim(),
    department: form.department.trim() || null,
    hireDate: form.hireDate,
    salary: form.salary === '' ? null : Number(form.salary),
  }

  let saved
  try {
    saved = isEdit.value
      ? await employeesApi.update(props.employee.id, payload)
      : await employeesApi.create(payload)
  } catch (e) {
    Object.assign(errors, getFieldErrors(e))
    formError.value = getErrorMessage(e, 'Unable to save the employee.')
    saving.value = false
    return
  }

  const fileErrors = []
  if (photoFile.value) {
    try {
      saved = await employeesApi.uploadPhoto(saved.id, photoFile.value)
    } catch (e) {
      fileErrors.push(`Photo: ${getErrorMessage(e, 'upload failed')}`)
    }
  }
  if (cvFile.value) {
    try {
      saved = await employeesApi.uploadCv(saved.id, cvFile.value)
    } catch (e) {
      fileErrors.push(`CV: ${getErrorMessage(e, 'upload failed')}`)
    }
  }

  saving.value = false
  emit('saved', { employee: saved, created: !isEdit.value, fileErrors })
}
</script>

<template>
  <AppModal
    :open="open"
    :title="isEdit ? 'Edit employee' : 'Add employee'"
    width="680px"
    :busy="saving"
    @close="emit('close')"
  >
    <div v-if="formError" class="alert alert-error" role="alert">{{ formError }}</div>

    <form id="employee-form" novalidate @submit.prevent="submit">
      <h3 class="section">Personal information</h3>
      <div class="grid-2">
        <div class="field">
          <label for="e-first" class="required">First name</label>
          <input id="e-first" v-model="form.firstName" class="input" :class="{ 'input-error': errors.firstName }" maxlength="100" />
          <span v-if="errors.firstName" class="field-error">{{ errors.firstName }}</span>
        </div>
        <div class="field">
          <label for="e-last" class="required">Last name</label>
          <input id="e-last" v-model="form.lastName" class="input" :class="{ 'input-error': errors.lastName }" maxlength="100" />
          <span v-if="errors.lastName" class="field-error">{{ errors.lastName }}</span>
        </div>
        <div class="field">
          <label for="e-email" class="required">Email</label>
          <input id="e-email" v-model="form.email" type="email" class="input" :class="{ 'input-error': errors.email }" maxlength="150" />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>
        <div class="field">
          <label for="e-phone">Phone</label>
          <input id="e-phone" v-model="form.phone" type="tel" class="input" :class="{ 'input-error': errors.phone }" placeholder="+212 600 000 000" maxlength="30" />
          <span v-if="errors.phone" class="field-error">{{ errors.phone }}</span>
        </div>
      </div>

      <h3 class="section">Job information</h3>
      <div class="grid-2">
        <div class="field">
          <label for="e-job" class="required">Job title</label>
          <input id="e-job" v-model="form.jobTitle" class="input" :class="{ 'input-error': errors.jobTitle }" maxlength="100" />
          <span v-if="errors.jobTitle" class="field-error">{{ errors.jobTitle }}</span>
        </div>
        <div class="field">
          <label for="e-dept">Department</label>
          <input id="e-dept" v-model="form.department" class="input" :class="{ 'input-error': errors.department }" maxlength="100" />
          <span v-if="errors.department" class="field-error">{{ errors.department }}</span>
        </div>
        <div class="field">
          <label for="e-hire" class="required">Hire date</label>
          <input id="e-hire" v-model="form.hireDate" type="date" class="input" :class="{ 'input-error': errors.hireDate }" />
          <span v-if="errors.hireDate" class="field-error">{{ errors.hireDate }}</span>
        </div>
        <div class="field">
          <label for="e-salary">Annual salary (MAD)</label>
          <input id="e-salary" v-model="form.salary" type="number" min="0" step="0.01" class="input" :class="{ 'input-error': errors.salary }" />
          <span v-if="errors.salary" class="field-error">{{ errors.salary }}</span>
        </div>
      </div>

      <h3 class="section">Documents</h3>
      <div class="field">
        <label>Photo</label>
        <div class="photo-row">
          <img v-if="photoPreview" :src="photoPreview" alt="" class="photo-preview" />
          <EmployeePhoto
            v-else
            :url="employee?.photoUrl"
            :name="`${form.firstName} ${form.lastName}`"
            :version="employee?.updatedAt"
            :size="56"
          />
          <div class="photo-picker">
            <FilePicker
              :accept="PHOTO_RULES.accept"
              :hint="PHOTO_RULES.hint"
              icon="upload"
              :file="photoFile"
              :current-label="employee?.hasPhoto ? 'Current photo' : ''"
              :error="errors.photo"
              @select="selectPhoto"
              @clear="photoFile = null; setPhotoPreview(null)"
            />
          </div>
        </div>
      </div>

      <div class="field">
        <label>CV</label>
        <FilePicker
          :accept="CV_RULES.accept"
          :hint="CV_RULES.hint"
          icon="fileText"
          :file="cvFile"
          :current-label="employee?.hasCv ? employee.cvFileName : ''"
          :error="errors.cv"
          @select="selectCv"
          @clear="cvFile = null"
        />
      </div>
    </form>

    <template #footer>
      <button class="btn btn-secondary" :disabled="saving" @click="emit('close')">Cancel</button>
      <button type="submit" form="employee-form" class="btn btn-primary" :disabled="saving">
        <span v-if="saving" class="spinner" />
        {{ saving ? 'Saving…' : isEdit ? 'Save changes' : 'Add employee' }}
      </button>
    </template>
  </AppModal>
</template>

<style scoped>
.section {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  margin: 4px 0 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border);
}
.section:not(:first-child) {
  margin-top: 8px;
}
.photo-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.photo-picker {
  flex: 1;
  min-width: 0;
}
.photo-preview {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
</style>
