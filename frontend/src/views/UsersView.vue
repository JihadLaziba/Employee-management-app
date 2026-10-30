<script setup>
import { onMounted, ref } from 'vue'
import AppIcon from '@/components/AppIcon.vue'
import SearchInput from '@/components/SearchInput.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import DataState from '@/components/DataState.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import UserFormModal from '@/components/users/UserFormModal.vue'
import UserPermissionsModal from '@/components/users/UserPermissionsModal.vue'
import UserDetailsModal from '@/components/users/UserDetailsModal.vue'
import { usersApi } from '@/api/users'
import { getErrorMessage } from '@/api/http'
import { usePagedList } from '@/composables/usePagedList'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { initials } from '@/utils/format'

const auth = useAuthStore()
const toast = useToastStore()

const list = usePagedList((params) => usersApi.list(params))
const { items, page, size, totalElements, totalPages, search, loading, error, loaded } = list

const formOpen = ref(false)
const permissionsOpen = ref(false)
const detailsOpen = ref(false)
const deleteOpen = ref(false)
const selected = ref(null)
const deleting = ref(false)

function openCreate() {
  selected.value = null
  formOpen.value = true
}
function openEdit(user) {
  selected.value = user
  formOpen.value = true
}
function openPermissions(user) {
  selected.value = user
  permissionsOpen.value = true
}
function openDetails(user) {
  selected.value = user
  detailsOpen.value = true
}
function openDelete(user) {
  selected.value = user
  deleteOpen.value = true
}

function onSaved(user) {
  const created = !selected.value
  formOpen.value = false
  toast.success(created ? `User ${user.name} created` : `User ${user.name} updated`)
  list.load()
}

function onPermissionsSaved(user) {
  permissionsOpen.value = false
  toast.success(`Permissions updated for ${user.name}`)
  list.load()
}

async function confirmDelete() {
  deleting.value = true
  try {
    await usersApi.remove(selected.value.id)
    toast.success(`User ${selected.value.name} deleted`)
    deleteOpen.value = false
    list.load()
  } catch (e) {
    toast.error(getErrorMessage(e, 'Unable to delete the user.'))
  } finally {
    deleting.value = false
  }
}

const isSelf = (user) => user.id === auth.user?.id

onMounted(list.load)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1 class="page-title">Users</h1>
        <p class="page-subtitle">Manage who can access the application and what they can do.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">
        <AppIcon name="plus" :size="16" />
        New user
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <SearchInput v-model="search" placeholder="Search by name or email" />
        <span v-if="loading && loaded" class="spinner toolbar-spinner" aria-label="Loading" />
      </div>

      <DataState
        :loading="loading"
        :loaded="loaded"
        :error="error"
        :empty="items.length === 0"
        :columns="6"
        :empty-title="search ? 'No users match your search' : 'No users yet'"
        :empty-text="search ? 'Try another name or email.' : 'Create the first user to get started.'"
        @retry="list.load"
      >
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>User type</th>
                <th>Permissions</th>
                <th>Status</th>
                <th class="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in items" :key="user.id">
                <td>
                  <div class="cell-user">
                    <span class="avatar-sm">{{ initials(user.name) }}</span>
                    <span class="cell-strong">{{ user.name }}</span>
                    <span v-if="isSelf(user)" class="badge">You</span>
                  </div>
                </td>
                <td>{{ user.email }}</td>
                <td>
                  <span class="badge" :class="user.role === 'SUPER_ADMIN' ? 'badge-primary' : ''">
                    {{ user.role === 'SUPER_ADMIN' ? 'Super Admin' : 'User' }}
                  </span>
                </td>
                <td>
                  <div class="perm-badges">
                    <span v-if="user.role === 'SUPER_ADMIN'" class="badge badge-primary">All</span>
                    <template v-else-if="user.permissions.length">
                      <span v-for="p in user.permissions" :key="p" class="badge">{{ p }}</span>
                    </template>
                    <span v-else class="text-muted">None</span>
                  </div>
                </td>
                <td>
                  <span class="badge" :class="user.enabled ? 'badge-success' : 'badge-danger'">
                    {{ user.enabled ? 'Active' : 'Disabled' }}
                  </span>
                </td>
                <td class="col-actions">
                  <div class="actions">
                    <button class="icon-btn" title="View" aria-label="View" @click="openDetails(user)">
                      <AppIcon name="eye" :size="17" />
                    </button>
                    <button class="icon-btn" title="Edit" aria-label="Edit" @click="openEdit(user)">
                      <AppIcon name="edit" :size="17" />
                    </button>
                    <button
                      class="icon-btn"
                      :title="user.role === 'SUPER_ADMIN' ? 'Super admins have all permissions' : 'Permissions'"
                      aria-label="Permissions"
                      :disabled="user.role === 'SUPER_ADMIN'"
                      @click="openPermissions(user)"
                    >
                      <AppIcon name="shield" :size="17" />
                    </button>
                    <button
                      class="icon-btn danger"
                      :title="isSelf(user) ? 'You cannot delete your own account' : 'Delete'"
                      aria-label="Delete"
                      :disabled="isSelf(user)"
                      @click="openDelete(user)"
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

    <UserFormModal :open="formOpen" :user="selected" @close="formOpen = false" @saved="onSaved" />
    <UserPermissionsModal
      :open="permissionsOpen"
      :user="selected"
      @close="permissionsOpen = false"
      @saved="onPermissionsSaved"
    />
    <UserDetailsModal :open="detailsOpen" :user="selected" @close="detailsOpen = false" />
    <ConfirmDialog
      :open="deleteOpen"
      title="Delete user"
      :message="selected ? `Delete ${selected.name}? This user will no longer be able to sign in. This action cannot be undone.` : ''"
      :loading="deleting"
      @cancel="deleteOpen = false"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.perm-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
