import { onBeforeUnmount, onMounted, ref } from 'vue'

export const IDLE_TIMEOUT_MS = 3 * 60 * 1000
export const WARNING_DURATION_MS = 60 * 1000
const REFRESH_THROTTLE_MS = 30 * 1000

const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'wheel']
const CHANNEL_NAME = 'ems-session'

export function useSessionTimeout({ refresh, onTimeout, onLogout }) {
  const warningVisible = ref(false)
  const secondsLeft = ref(Math.round(WARNING_DURATION_MS / 1000))
  const extending = ref(false)

  let lastActivity = Date.now()
  let lastActivityBroadcast = 0
  let lastRefresh = 0
  let refreshing = false
  let timer = null
  let channel = null

  function broadcast(type) {
    try {
      channel?.postMessage({ type, at: Date.now() })
    } catch {
      
    }
  }

  async function doRefresh() {
    if (refreshing) return
    refreshing = true
    try {
      await refresh()
      lastRefresh = Date.now()
      broadcast('refreshed')
    } catch {
      
    } finally {
      refreshing = false
    }
  }

  function onActivity() {
    if (warningVisible.value) return
    lastActivity = Date.now()
    if (lastActivity - lastActivityBroadcast > 5000) {
      lastActivityBroadcast = lastActivity
      broadcast('activity')
    }
    if (Date.now() - lastRefresh > REFRESH_THROTTLE_MS) {
      doRefresh()
    }
  }

  function tick() {
    const idle = Date.now() - lastActivity
    if (idle >= IDLE_TIMEOUT_MS + WARNING_DURATION_MS) {
      stop()
      broadcast('logout')
      onTimeout()
      return
    }
    if (idle >= IDLE_TIMEOUT_MS) {
      warningVisible.value = true
      secondsLeft.value = Math.max(0, Math.ceil((IDLE_TIMEOUT_MS + WARNING_DURATION_MS - idle) / 1000))
    }
  }

  
  async function stayConnected() {
    extending.value = true
    await doRefresh()
    extending.value = false
    lastActivity = Date.now()
    warningVisible.value = false
    broadcast('activity')
  }

  
  function logoutNow() {
    stop()
    broadcast('logout')
    onLogout()
  }

  function onChannelMessage(event) {
    const { type, at } = event.data ?? {}
    if (type === 'activity') {
      lastActivity = Math.max(lastActivity, at)
      warningVisible.value = false
    } else if (type === 'refreshed') {
      lastRefresh = Math.max(lastRefresh, at)
    } else if (type === 'logout') {
      stop()
      onTimeout()
    }
  }

  function start() {
    ACTIVITY_EVENTS.forEach((e) => window.addEventListener(e, onActivity, { passive: true }))
    timer = setInterval(tick, 1000)
    if ('BroadcastChannel' in window) {
      channel = new BroadcastChannel(CHANNEL_NAME)
      channel.onmessage = onChannelMessage
    }
    doRefresh()
  }

  function stop() {
    ACTIVITY_EVENTS.forEach((e) => window.removeEventListener(e, onActivity))
    clearInterval(timer)
    timer = null
    warningVisible.value = false
    channel?.close()
    channel = null
  }

  onMounted(start)
  onBeforeUnmount(stop)

  return { warningVisible, secondsLeft, extending, stayConnected, logoutNow }
}
