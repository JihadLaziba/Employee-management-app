<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { getErrorMessage } from '@/api/http'
import AppIcon from '@/components/AppIcon.vue'

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const formError = ref('')
const loading = ref(false)
const showPassword = ref(false)
const sessionExpired = route.query.expired === '1'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate() {
  errors.email = !form.email.trim()
    ? 'Email is required'
    : !EMAIL_PATTERN.test(form.email.trim())
      ? 'Email format is invalid'
      : ''
  errors.password = form.password ? '' : 'Password is required'
  return !errors.email && !errors.password
}

async function submit() {
  formError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await auth.login(form.email.trim(), form.password)
    toast.success(`Welcome, ${auth.user.name}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect.startsWith('/') ? redirect : '/')
  } catch (error) {
    formError.value = getErrorMessage(error, 'Login failed. Please try again.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card card">
      <div class="login-header">
        <span class="logo">
          <svg viewBox="0 0 32 32" width="44" height="44" aria-hidden="true">
            <rect width="32" height="32" rx="8" fill="currentColor" />
            <path d="M10 10h12M10 16h9M10 22h12" stroke="#fff" stroke-width="3" stroke-linecap="round" />
          </svg>
        </span>
        <h1>Sign in to EMS</h1>
        <p>Employee Management Application</p>
      </div>

      <div v-if="sessionExpired && !formError" class="alert alert-info">
        <AppIcon name="alert" :size="16" />
        Your session has expired. Please sign in again.
      </div>
      <div v-if="formError" class="alert alert-error" role="alert">
        <AppIcon name="alert" :size="16" />
        {{ formError }}
      </div>

      <form novalidate @submit.prevent="submit">
        <div class="field">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="input"
            :class="{ 'input-error': errors.email }"
            placeholder="you@company.com"
            autocomplete="username"
            autofocus
          />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label for="password">Password</label>
          <div class="password-wrapper">
            <input
              id="password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="input"
              :class="{ 'input-error': errors.password }"
              placeholder="••••••••"
              autocomplete="current-password"
            />
            <button
              type="button"
              class="toggle-password"
              :aria-label="showPassword ? 'Hide password' : 'Show password'"
              @click="showPassword = !showPassword"
            >
              <AppIcon :name="showPassword ? 'eyeOff' : 'eye'" :size="16" />
            </button>
          </div>
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          <span v-if="loading" class="spinner" />
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
  background:
    radial-gradient(circle at 15% 20%, var(--primary-soft), transparent 40%),
    radial-gradient(circle at 85% 80%, var(--primary-soft), transparent 40%),
    var(--bg);
}
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.logo {
  color: var(--primary);
  display: inline-flex;
  margin-bottom: 12px;
}
.login-header h1 {
  font-size: 22px;
  margin: 0 0 4px;
}
.login-header p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}
.password-wrapper {
  position: relative;
}
.password-wrapper .input {
  padding-right: 40px;
}
.toggle-password {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  display: flex;
}
.btn-block {
  width: 100%;
  margin-top: 8px;
}
</style>
