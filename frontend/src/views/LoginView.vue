<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { getErrorMessage } from '@/api/http'
import AppIcon from '@/components/AppIcon.vue'
import BrandMark from '@/components/BrandMark.vue'

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const formError = ref('')
const loading = ref(false)
const showPassword = ref(false)
const SESSION_MESSAGES = {
  idle: 'You were signed out after 3 minutes of inactivity. Please sign in again.',
  expired: 'Your session has expired. Please sign in again.',
}
const sessionMessage = SESSION_MESSAGES[route.query.reason] ?? ''

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
  <div class="login">
    <!-- Panneau de marque -->
    <section class="showcase" aria-hidden="true">
      <div class="showcase-brand">
        <BrandMark :size="38" />
        <span>EMS <em>People office</em></span>
      </div>

      <div class="showcase-copy">
        <h2>Every employee record, in one place.</h2>
        <p>Profiles, CVs and employment contracts, with access granted person by person.</p>
      </div>

      <!-- Pile de fiches du personnel -->
      <div class="files">
        <div class="file file-3"><span class="file-tab" /></div>
        <div class="file file-2"><span class="file-tab" /></div>
        <div class="file file-1">
          <span class="file-tab" />
          <div class="file-head">
            <span class="file-photo" />
            <span class="file-lines">
              <span class="line w-70" />
              <span class="line w-45 muted" />
            </span>
          </div>
          <div class="file-rows">
            <span class="row"><span class="line w-30 muted" /><span class="line w-50" /></span>
            <span class="row"><span class="line w-30 muted" /><span class="line w-40" /></span>
            <span class="row"><span class="line w-30 muted" /><span class="line w-55" /></span>
          </div>
          <div class="file-docs">
            <span class="doc">CV.pdf</span>
            <span class="doc">Contract.pdf</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Formulaire -->
    <section class="panel">
      <div class="form-wrap">
        <div class="mobile-brand">
          <BrandMark :size="36" />
          <span>EMS</span>
        </div>

        <h1>Sign in</h1>
        <p class="lead">Use the email and password provided by your administrator.</p>

        <div v-if="sessionMessage && !formError" class="alert alert-info">
          <AppIcon name="clock" :size="16" />
          {{ sessionMessage }}
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

        <p class="footnote">
          <AppIcon name="lock" :size="13" />
          Sessions end automatically after 3 minutes of inactivity.
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1fr) minmax(380px, 560px);
  background: var(--surface);
}

/* ---------- Panneau de marque ---------- */
.showcase {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  color: #fff;
  padding: 40px 56px;
  display: flex;
  flex-direction: column;
}
.showcase-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  font-size: 17px;
}
.showcase-brand em {
  font-style: normal;
  font-weight: 400;
  color: var(--ink-text);
  margin-left: 4px;
}
.showcase-copy {
  margin-top: auto;
  max-width: 560px;
  position: relative;
  z-index: 2;
}
.showcase-copy h2 {
  font-size: clamp(30px, 3.2vw, 42px);
  font-weight: 600;
  line-height: 1.12;
  letter-spacing: -0.02em;
  margin: 0 0 14px;
  max-width: 15ch;
}
.showcase-copy p {
  margin: 0;
  max-width: 42ch;
  color: var(--ink-text);
  font-size: 16px;
  line-height: 1.55;
}

/* Pile de fiches */
.files {
  position: relative;
  height: 300px;
  margin: 48px 0 0;
}
.file {
  position: absolute;
  left: 0;
  top: 0;
  width: 340px;
  height: 230px;
  border-radius: 10px;
  background: var(--ink-soft);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.file-tab {
  position: absolute;
  top: -10px;
  left: 22px;
  width: 64px;
  height: 12px;
  border-radius: 6px 6px 0 0;
  background: inherit;
  border: inherit;
  border-bottom: none;
}
.file-3 {
  transform: translate(120px, 0) rotate(7deg);
  opacity: 0.45;
}
.file-2 {
  transform: translate(60px, 18px) rotate(3deg);
  opacity: 0.7;
}
.file-1 {
  transform: translate(0, 40px) rotate(-2deg);
  background: #fff;
  border-color: #fff;
  box-shadow: 0 30px 60px -20px rgba(0, 0, 0, 0.55);
  padding: 22px 24px;
  color: var(--ink);
}
.file-1 .file-tab {
  background: var(--brass);
  border-color: var(--brass);
}
.file-head {
  display: flex;
  gap: 14px;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
.file-photo {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--primary-soft);
  border: 2px solid var(--primary);
}
.file-lines,
.file-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.file-rows {
  padding: 16px 0 14px;
}
.row {
  display: flex;
  gap: 14px;
}
.line {
  display: block;
  height: 7px;
  border-radius: 4px;
  background: var(--ink);
  opacity: 0.85;
}
.line.muted {
  background: var(--border-strong);
  opacity: 1;
}
.w-30 { width: 30%; }
.w-40 { width: 40%; }
.w-45 { width: 45%; }
.w-50 { width: 50%; }
.w-55 { width: 55%; }
.w-70 { width: 70%; }
.file-docs {
  display: flex;
  gap: 8px;
}
.doc {
  font-size: 11.5px;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  background: var(--brass-soft);
  color: #6b4a14;
}

/* ---------- Formulaire ---------- */
.panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
}
.form-wrap {
  width: 100%;
  max-width: 380px;
}
.mobile-brand {
  display: none;
}
h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 6px;
}
.lead {
  margin: 0 0 28px;
  color: var(--text-muted);
  font-size: 15px;
}
.field label {
  font-size: 13.5px;
  font-weight: 500;
}
.input {
  height: 44px;
}
.password-wrapper {
  position: relative;
}
.password-wrapper .input {
  padding-right: 44px;
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
  padding: 8px;
  display: flex;
  border-radius: var(--radius-sm);
}
.btn-block {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  font-size: 15px;
}
.footnote {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 28px 0 0;
  font-size: 12.5px;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .login {
    grid-template-columns: 1fr;
  }
  .showcase {
    display: none;
  }
  .panel {
    min-height: 100vh;
    background: var(--bg);
  }
  .form-wrap {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    padding: 28px 24px;
  }
  .mobile-brand {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 700;
    font-size: 17px;
    margin-bottom: 22px;
  }
}
</style>
