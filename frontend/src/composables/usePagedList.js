import { onBeforeUnmount, ref, watch } from 'vue'
import { getErrorMessage } from '@/api/http'

export function usePagedList(fetcher, { pageSize = 10, debounceMs = 350 } = {}) {
  const items = ref([])
  const page = ref(0)
  const size = ref(pageSize)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const search = ref('')
  const loading = ref(false)
  const error = ref('')
  const loaded = ref(false)

  let requestId = 0
  let timer = null

  async function load() {
    const id = ++requestId
    loading.value = true
    error.value = ''
    try {
      const data = await fetcher({ search: search.value.trim(), page: page.value, size: size.value })
      if (id !== requestId) return 
      if (data.content.length === 0 && page.value > 0 && data.totalPages > 0) {
        page.value = data.totalPages - 1
        return load()
      }

      items.value = data.content
      totalElements.value = data.totalElements
      totalPages.value = data.totalPages
      loaded.value = true
    } catch (e) {
      if (id !== requestId) return
      error.value = getErrorMessage(e, 'Unable to load data. Please try again.')
    } finally {
      if (id === requestId) loading.value = false
    }
  }

  watch(search, () => {
    clearTimeout(timer)
    timer = setTimeout(() => {
      page.value = 0
      load()
    }, debounceMs)
  })

  function goToPage(p) {
    page.value = p
    load()
  }

  function setPageSize(s) {
    size.value = s
    page.value = 0
    load()
  }

  onBeforeUnmount(() => clearTimeout(timer))

  return { items, page, size, totalElements, totalPages, search, loading, error, loaded, load, goToPage, setPageSize }
}
