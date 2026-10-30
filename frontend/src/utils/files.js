import { getErrorMessage } from '@/api/http'

export const PHOTO_RULES = {
  types: ['image/jpeg', 'image/png', 'image/webp'],
  maxSize: 2 * 1024 * 1024,
  accept: 'image/jpeg,image/png,image/webp',
  hint: 'JPEG, PNG or WEBP – max 2 MB',
}

export const CV_RULES = {
  types: ['application/pdf'],
  maxSize: 5 * 1024 * 1024,
  accept: 'application/pdf,.pdf',
  hint: 'PDF – max 5 MB',
}

export function validatePhoto(file) {
  if (!file) return ''
  if (!PHOTO_RULES.types.includes(file.type)) return 'Photo must be a JPEG, PNG or WEBP image'
  if (file.size > PHOTO_RULES.maxSize) return 'Photo must not exceed 2 MB'
  return ''
}

export function validateCv(file) {
  if (!file) return ''
  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')
  if (!isPdf) return 'CV must be a PDF file'
  if (file.size > CV_RULES.maxSize) return 'CV must not exceed 5 MB'
  return ''
}

export function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function fileNameFromResponse(response, fallback) {
  const header = response.headers?.['content-disposition'] ?? ''
  const utf8 = header.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8) return decodeURIComponent(utf8[1])
  const plain = header.match(/filename="?([^";]+)"?/i)
  return plain ? plain[1] : fallback
}

export function saveBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export async function openBlobInNewTab(loader) {
  const tab = window.open('', '_blank')
  try {
    const response = await loader()
    const url = URL.createObjectURL(response.data)
    if (tab) {
      tab.location.href = url
    } else {
      window.location.href = url
    }
    setTimeout(() => URL.revokeObjectURL(url), 60_000)
  } catch (e) {
    tab?.close()
    throw e
  }
}

export async function getBlobErrorMessage(error, fallback) {
  const data = error?.response?.data
  if (data instanceof Blob) {
    try {
      const json = JSON.parse(await data.text())
      if (json?.message) return json.message
    } catch {
    }
  }
  return getErrorMessage(error, fallback)
}
