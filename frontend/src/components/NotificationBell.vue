<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import { notificationsApi } from '@/api/notifications'
import { getErrorMessage } from '@/api/http'
import { formatWhen, initials, timeAgo } from '@/utils/format'

/** Fréquence de vérification des nouvelles notifications */
const POLL_INTERVAL_MS = 30_000
const PAGE_SIZE = 15

const open = ref(false)
const unread = ref(0)
const items = ref([])
const page = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const root = ref(null)

let pollTimer = null

/** Catégorie visuelle de chaque action (couleur et icône) */
const CATEGORY = {
  LOGIN: { tone: 'neutral', icon: 'login' },
  LOGOUT: { tone: 'neutral', icon: 'logout' },
  SESSION_EXPIRED: { tone: 'neutral', icon: 'clock' },
  USER_CREATED: { tone: 'success', icon: 'users' },
  USER_UPDATED: { tone: 'info', icon: 'users' },
  USER_DELETED: { tone: 'danger', icon: 'users' },
  PERMISSIONS_UPDATED: { tone: 'warning', icon: 'shield' },
  EMPLOYEE_CREATED: { tone: 'success', icon: 'briefcase' },
  EMPLOYEE_UPDATED: { tone: 'info', icon: 'briefcase' },
  EMPLOYEE_DELETED: { tone: 'danger', icon: 'briefcase' },
  PHOTO_UPLOADED: { tone: 'info', icon: 'upload' },
  CV_UPLOADED: { tone: 'info', icon: 'upload' },
  CV_ACCESSED: { tone: 'neutral', icon: 'fileText' },
  CONTRACT_GENERATED: { tone: 'primary', icon: 'file' },
}
const category = (action) => CATEGORY[action] ?? { tone: 'neutral', icon: 'alert' }

const badge = computed(() => (unread.value > 99 ? '99+' : String(unread.value)))

async function refreshCount() {
  if (document.hidden) return // pas de requêtes quand l'onglet est en arrière-plan
  try {
    unread.value = await notificationsApi.unreadCount()
  } catch {
    /* silencieux : la cloche n'est pas critique */
  }
}

async function loadFirstPage() {
  loading.value = true
  error.value = ''
  try {
    const data = await notificationsApi.list({ page: 0, size: PAGE_SIZE })
    items.value = data.content
    page.value = 0
    hasMore.value = !data.last
    // L'ouverture du panneau vaut lecture : le badge disparaît,
    // mais les éléments restent mis en évidence pendant cette consultation.
    if (unread.value > 0) {
      await notificationsApi.markAllRead()
      unread.value = 0
    }
  } catch (e) {
    error.value = getErrorMessage(e, 'Unable to load notifications.')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    const data = await notificationsApi.list({ page: page.value + 1, size: PAGE_SIZE })
    items.value.push(...data.content)
    page.value += 1
    hasMore.value = !data.last
  } catch (e) {
    error.value = getErrorMessage(e, 'Unable to load more notifications.')
  } finally {
    loadingMore.value = false
  }
}

function toggle() {
  open.value = !open.value
  if (open.value) loadFirstPage()
}

function close() {
  open.value = false
}

function onClickOutside(e) {
  if (open.value && root.value && !root.value.contains(e.target)) close()
}
function onKeydown(e) {
  if (e.key === 'Escape') close()
}
function onVisibility() {
  if (!document.hidden) refreshCount()
}

onMounted(() => {
  refreshCount()
  pollTimer = setInterval(refreshCount, POLL_INTERVAL_MS)
  document.addEventListener('mousedown', onClickOutside)
  document.addEventListener('keydown', onKeydown)
  document.addEventListener('visibilitychange', onVisibility)
})
onBeforeUnmount(() => {
  clearInterval(pollTimer)
  document.removeEventListener('mousedown', onClickOutside)
  document.removeEventListener('keydown', onKeydown)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<template>
  <div ref="root" class="bell">
    <button
      class="bell-btn"
      :class="{ active: open }"
      :aria-label="unread ? `Notifications, ${unread} unread` : 'Notifications'"
      :aria-expanded="open"
      @click="toggle"
    >
      <AppIcon name="bell" :size="20" />
      <span v-if="unread > 0" class="bell-badge">{{ badge }}</span>
    </button>

    <Transition name="pop">
      <div v-if="open" class="panel" role="dialog" aria-label="Notifications">
        <header class="panel-header">
          <div>
            <h3>Activity</h3>
            <p>What users did in the application</p>
          </div>
          <button class="icon-btn" aria-label="Close" @click="close"><AppIcon name="x" :size="16" /></button>
        </header>

        <div class="panel-body">
          <!-- Chargement -->
          <div v-if="loading" class="panel-state">
            <span class="spinner" />
          </div>

          <!-- Erreur -->
          <div v-else-if="error && items.length === 0" class="panel-state">
            <p>{{ error }}</p>
            <button class="btn btn-secondary btn-sm" @click="loadFirstPage">Try again</button>
          </div>

          <!-- Vide -->
          <div v-else-if="items.length === 0" class="panel-state">
            <span class="empty-icon"><AppIcon name="bell" :size="22" /></span>
            <p>No activity yet</p>
          </div>

          <!-- Liste -->
          <ul v-else class="list">
            <li v-for="n in items" :key="n.id" class="item" :class="{ unread: n.unread }">
              <span class="item-avatar" :class="`tone-${category(n.action).tone}`">
                {{ initials(n.actorName) }}
                <span class="item-icon"><AppIcon :name="category(n.action).icon" :size="11" /></span>
              </span>
              <div class="item-body">
                <p class="item-text">
                  <strong>{{ n.own ? 'You' : n.actorName }}</strong> {{ n.message }}
                </p>
                <p v-if="n.details" class="item-details">{{ n.details }}</p>
                <time class="item-time" :datetime="n.createdAt" :title="timeAgo(n.createdAt)">
                  {{ formatWhen(n.createdAt) }}
                </time>
              </div>
              <span v-if="n.unread" class="item-dot" aria-label="New" />
            </li>
          </ul>
        </div>

        <footer v-if="hasMore && !loading" class="panel-footer">
          <button class="btn btn-ghost btn-sm" :disabled="loadingMore" @click="loadMore">
            <span v-if="loadingMore" class="spinner" />
            Load older activity
          </button>
        </footer>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.bell {
  position: relative;
}
.bell-btn {
  position: relative;
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.bell-btn:hover,
.bell-btn.active {
  background: var(--surface-hover);
  color: var(--text);
}
.bell-badge {
  position: absolute;
  top: 3px;
  left: 21px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  border: 2px solid var(--surface);
  box-sizing: content-box;
}

.panel {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 400px;
  max-width: calc(100vw - 24px);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: 60;
  display: flex;
  flex-direction: column;
  max-height: min(560px, calc(100vh - 90px));
}
.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 14px 16px 12px;
  border-bottom: 1px solid var(--border);
}
.panel-header h3 {
  margin: 0;
  font-size: 15px;
}
.panel-header p {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--text-muted);
}
.panel-body {
  overflow-y: auto;
  flex: 1;
}
.panel-state {
  padding: 36px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--text-muted);
  text-align: center;
}
.panel-state p {
  margin: 0;
}
.empty-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--surface-hover);
}
.list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.item {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
}
.item:last-child {
  border-bottom: none;
}
.item.unread {
  background: var(--primary-soft);
  background: color-mix(in srgb, var(--primary) 6%, var(--surface));
}
.item-avatar {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.item-icon {
  position: absolute;
  right: -3px;
  bottom: -3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-secondary);
}
.tone-neutral {
  background: var(--surface-hover);
  color: var(--text-secondary);
}
.tone-success {
  background: var(--success-soft);
  color: var(--success);
}
.tone-info {
  background: var(--info-soft);
  color: var(--info);
}
.tone-warning {
  background: var(--warning-soft);
  color: var(--warning);
}
.tone-danger {
  background: var(--danger-soft);
  color: var(--danger);
}
.tone-primary {
  background: var(--primary-soft);
  color: var(--primary);
}
.item-body {
  flex: 1;
  min-width: 0;
}
.item-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  color: var(--text-secondary);
}
.item-text strong {
  color: var(--text);
}
.item-details {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-time {
  display: block;
  margin-top: 3px;
  font-size: 11.5px;
  color: var(--text-muted);
}
.item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 6px;
  flex-shrink: 0;
}
.panel-footer {
  border-top: 1px solid var(--border);
  padding: 8px;
  display: flex;
  justify-content: center;
}
.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
@media (max-width: 560px) {
  .panel {
    position: fixed;
    left: 12px;
    right: 12px;
    top: calc(var(--navbar-height) + 4px);
    width: auto;
  }
}
</style>
