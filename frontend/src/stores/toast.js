import { defineStore } from 'pinia'

let nextId = 1

export const useToastStore = defineStore('toast', {
  state: () => ({
    items: [],
  }),

  actions: {
    push(type, message, duration = 4000) {
      const id = nextId++
      this.items.push({ id, type, message })
      setTimeout(() => this.dismiss(id), duration)
    },
    success(message) {
      this.push('success', message)
    },
    error(message) {
      this.push('error', message, 6000)
    },
    dismiss(id) {
      this.items = this.items.filter((t) => t.id !== id)
    },
  },
})
