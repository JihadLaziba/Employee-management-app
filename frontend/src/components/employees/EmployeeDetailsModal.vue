<script setup>
import AppModal from '@/components/AppModal.vue'
import AppIcon from '@/components/AppIcon.vue'
import EmployeePhoto from './EmployeePhoto.vue'
import { formatDate, formatDateTime, formatMoney } from '@/utils/format'

defineProps({
  open: { type: Boolean, default: false },
  employee: { type: Object, default: null },
  canEdit: { type: Boolean, default: false },
  busy: { type: String, default: '' },
})
const emit = defineEmits(['close', 'view-cv', 'download-cv', 'download-contract', 'edit'])
</script>

<template>
  <AppModal :open="open" title="Employee details" width="560px" @close="emit('close')">
    <template v-if="employee">
      <div class="head">
        <EmployeePhoto :url="employee.photoUrl" :name="employee.fullName" :version="employee.updatedAt" :size="72" />
        <div>
          <h3>{{ employee.fullName }}</h3>
          <p>{{ employee.jobTitle }}<template v-if="employee.department"> · {{ employee.department }}</template></p>
        </div>
      </div>

      <dl class="details">
        <dt>Email</dt>
        <dd>{{ employee.email }}</dd>
        <dt>Phone</dt>
        <dd>{{ employee.phone || '—' }}</dd>
        <dt>Department</dt>
        <dd>{{ employee.department || '—' }}</dd>
        <dt>Hire date</dt>
        <dd>{{ formatDate(employee.hireDate) }}</dd>
        <dt>Annual salary</dt>
        <dd>{{ formatMoney(employee.salary) }}</dd>
        <dt>Last updated</dt>
        <dd>{{ formatDateTime(employee.updatedAt) }}</dd>
      </dl>

      <div class="documents">
        <h4>Documents</h4>

        <div class="doc">
          <AppIcon name="fileText" :size="20" class="doc-icon" />
          <div class="doc-text">
            <strong>CV</strong>
            <small>{{ employee.hasCv ? employee.cvFileName : 'No CV uploaded' }}</small>
          </div>
          <template v-if="employee.hasCv">
            <button class="btn btn-secondary btn-sm" :disabled="!!busy" @click="emit('view-cv', employee)">
              <AppIcon name="eye" :size="14" /> View
            </button>
            <button class="btn btn-secondary btn-sm" :disabled="!!busy" @click="emit('download-cv', employee)">
              <span v-if="busy === 'cv'" class="spinner" />
              <AppIcon v-else name="download" :size="14" /> Download
            </button>
          </template>
        </div>

        <div class="doc">
          <AppIcon name="file" :size="20" class="doc-icon" />
          <div class="doc-text">
            <strong>Employment contract</strong>
            <small>Generated automatically from the contract template</small>
          </div>
          <button class="btn btn-primary btn-sm" :disabled="!!busy" @click="emit('download-contract', employee)">
            <span v-if="busy === 'contract'" class="spinner" />
            <AppIcon v-else name="download" :size="14" /> Generate
          </button>
        </div>
      </div>
    </template>

    <template #footer>
      <button class="btn btn-secondary" @click="emit('close')">Close</button>
      <button v-if="canEdit && employee" class="btn btn-primary" @click="emit('edit', employee)">
        <AppIcon name="edit" :size="16" /> Edit
      </button>
    </template>
  </AppModal>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.head h3 {
  margin: 0;
  font-size: 18px;
}
.head p {
  margin: 2px 0 0;
  color: var(--text-muted);
}
.documents {
  margin-top: 24px;
}
.documents h4 {
  margin: 0 0 10px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
}
.doc {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.doc-icon {
  color: var(--primary);
}
.doc-text {
  flex: 1;
  min-width: 160px;
  display: flex;
  flex-direction: column;
}
.doc-text small {
  color: var(--text-muted);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
