<script setup>
import { computed, onMounted, ref } from 'vue'
import AppIcon from '@/components/AppIcon.vue'
import SearchInput from '@/components/SearchInput.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import DataState from '@/components/DataState.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import EmployeePhoto from '@/components/employees/EmployeePhoto.vue'
import EmployeeFormModal from '@/components/employees/EmployeeFormModal.vue'
import EmployeeDetailsModal from '@/components/employees/EmployeeDetailsModal.vue'
import { employeesApi } from '@/api/employees'
import { getErrorMessage } from '@/api/http'
import { usePagedList } from '@/composables/usePagedList'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { formatDate } from '@/utils/format'
import { fileNameFromResponse, getBlobErrorMessage, openBlobInNewTab, saveBlob } from '@/utils/files'

const auth = useAuthStore()
const toast = useToastStore()

const canCreate = computed(() => auth.can('CREATE'))
const canUpdate = computed(() => auth.can('UPDATE'))
const canDelete = computed(() => auth.can('DELETE'))

const list = usePagedList((params) => employeesApi.list(params))
const { items, page, size, totalElements, totalPages, search, loading, error, loaded } = list

const formOpen = ref(false)
const detailsOpen = ref(false)
const deleteOpen = ref(false)
const selected = ref(null)
const deleting = ref(false)
const busy = ref('')
const busyId = ref(null)

function openCreate() {
  selected.value = null
  formOpen.value = true
}
function openEdit(employee) {
  selected.value = employee
  detailsOpen.value = false
  formOpen.value = true
}
function openDetails(employee) {
  selected.value = employee
  detailsOpen.value = true
}
function openDelete(employee) {
  selected.value = employee
  deleteOpen.value = true
}

function onSaved({ employee, created, fileErrors }) {
  formOpen.value = false
  if (fileErrors.length) {
    toast.success(`${employee.fullName} ${created ? 'created' : 'updated'}`)
    fileErrors.forEach((msg) => toast.error(msg))
  } else {
    toast.success(created ? `Employee ${employee.fullName} added` : `Employee ${employee.fullName} updated`)
  }
  list.load()
}

async function confirmDelete() {
  deleting.value = true
  try {
    await employeesApi.remove(selected.value.id)
    toast.success(`Employee ${selected.value.fullName} deleted`)
    deleteOpen.value = false
    list.load()
  } catch (e) {
    toast.error(getErrorMessage(e, 'Unable to delete the employee.'))
  } finally {
    deleting.value = false
  }
}

async function runDownload(kind, employee, task) {
  busy.value = kind
  busyId.value = employee.id
  try {
    await task()
  } catch (e) {
    toast.error(await getBlobErrorMessage(e, 'Unable to download the file.'))
  } finally {
    busy.value = ''
    busyId.value = null
  }
}

function downloadCv(employee) {
  return runDownload('cv', employee, async () => {
    const response = await employeesApi.fetchBlob(employee.cvUrl)
    saveBlob(response.data, fileNameFromResponse(response, employee.cvFileName || 'cv.pdf'))
  })
}

async function viewCv(employee) {
  try {
    await openBlobInNewTab(() => employeesApi.fetchBlob(employee.cvUrl))
  } catch (e) {
    toast.error(await getBlobErrorMessage(e, 'Unable to open the CV.'))
  }
}

function downloadContract(employee) {
  return runDownload('contract', employee, async () => {
    const response = await employeesApi.contract(employee.id)
    saveBlob(response.data, fileNameFromResponse(response, `contract-${employee.id}.pdf`))
    toast.success(`Contract generated for ${employee.fullName}`)
  })
}

const isBusy = (kind, employee) => busy.value === kind && busyId.value === employee.id

onMounted(list.load)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1 class="page-title">Employees</h1>
        <p class="page-subtitle">Manage employee records, documents and contracts.</p>
      </div>
      <button v-if="canCreate" class="btn btn-primary" @click="openCreate">
        <AppIcon name="plus" :size="16" />
        Add employee
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <SearchInput v-model="search" placeholder="Search by name, email, job or department" />
        <span v-if="loading && loaded" class="spinner toolbar-spinner" aria-label="Loading" />
      </div>

      <DataState
        :loading="loading"
        :loaded="loaded"
        :error="error"
        :empty="items.length === 0"
        :columns="6"
        :empty-title="search ? 'No employees match your search' : 'No employees yet'"
        :empty-text="
          search
            ? 'Try another name, email, job title or department.'
            : canCreate
              ? 'Add the first employee to get started.'
              : 'Employees will appear here once they are added.'
        "
        @retry="list.load"
      >
        <template #empty-action>
          <button v-if="canCreate && !search" class="btn btn-primary" @click="openCreate">
            <AppIcon name="plus" :size="16" />
            Add employee
          </button>
        </template>

        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Employee</th>
                <th>Job title</th>
                <th>Department</th>
                <th>Hire date</th>
                <th>CV</th>
                <th class="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="emp in items" :key="emp.id">
                <td>
                  <button class="cell-employee" @click="openDetails(emp)">
                    <EmployeePhoto :url="emp.photoUrl" :name="emp.fullName" :version="emp.updatedAt" :size="38" />
                    <span class="cell-employee-text">
                      <span class="cell-strong">{{ emp.fullName }}</span>
                      <span class="text-muted cell-email">{{ emp.email }}</span>
                    </span>
                  </button>
                </td>
                <td>{{ emp.jobTitle }}</td>
                <td>
                  <span v-if="emp.department" class="badge">{{ emp.department }}</span>
                  <span v-else class="text-muted">—</span>
                </td>
                <td>{{ formatDate(emp.hireDate) }}</td>
                <td>
                  <button
                    v-if="emp.hasCv"
                    class="link-btn"
                    :disabled="isBusy('cv', emp)"
                    :title="`Download ${emp.cvFileName}`"
                    @click="downloadCv(emp)"
                  >
                    <span v-if="isBusy('cv', emp)" class="spinner" />
                    <AppIcon v-else name="fileText" :size="15" />
                    PDF
                  </button>
                  <span v-else class="text-muted">—</span>
                </td>
                <td class="col-actions">
                  <div class="actions">
                    <button class="icon-btn" title="View details" aria-label="View" @click="openDetails(emp)">
                      <AppIcon name="eye" :size="17" />
                    </button>
                    <button
                      class="icon-btn"
                      title="Generate contract"
                      aria-label="Generate contract"
                      :disabled="isBusy('contract', emp)"
                      @click="downloadContract(emp)"
                    >
                      <span v-if="isBusy('contract', emp)" class="spinner" />
                      <AppIcon v-else name="file" :size="17" />
                    </button>
                    <button v-if="canUpdate" class="icon-btn" title="Edit" aria-label="Edit" @click="openEdit(emp)">
                      <AppIcon name="edit" :size="17" />
                    </button>
                    <button
                      v-if="canDelete"
                      class="icon-btn danger"
                      title="Delete"
                      aria-label="Delete"
                      @click="openDelete(emp)"
                    >
                      <AppIcon name="trash" :size="17" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </DataState>

      <PaginationBar
        v-if="!error && totalElements > 0"
        :page="page"
        :size="size"
        :total-elements="totalElements"
        :total-pages="totalPages"
        :disabled="loading"
        @change="list.goToPage"
        @size-change="list.setPageSize"
      />
    </div>

    <EmployeeFormModal :open="formOpen" :employee="selected" @close="formOpen = false" @saved="onSaved" />
    <EmployeeDetailsModal
      :open="detailsOpen"
      :employee="selected"
      :can-edit="canUpdate"
      :busy="selected && busyId === selected.id ? busy : ''"
      @close="detailsOpen = false"
      @edit="openEdit"
      @view-cv="viewCv"
      @download-cv="downloadCv"
      @download-contract="downloadContract"
    />
    <ConfirmDialog
      :open="deleteOpen"
      title="Delete employee"
      :message="
        selected
          ? `Delete ${selected.fullName}? Their photo and CV will also be removed. This action cannot be undone.`
          : ''
      "
      :loading="deleting"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.cell-employee {
  display: flex;
  align-items: center;
  gap: 12px;
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  color: inherit;
  text-align: left;
  cursor: pointer;
}
.cell-employee:hover .cell-strong {
  color: var(--primary);
}
.cell-employee-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.cell-email {
  font-size: 12px;
}
.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  padding: 4px 6px;
  margin-left: -6px;
  border-radius: 6px;
  font: inherit;
  font-weight: 600;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
}
.link-btn:hover:not(:disabled) {
  background: var(--primary-soft);
}
</style>
