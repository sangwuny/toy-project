<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { saveSession, type AuthSession } from '../utils/auth'
import { apiFetch } from '../utils/api'

type AuthMode = 'login' | 'signup'

const mode = ref<AuthMode>('login')
const loading = ref(false)
const error = ref('')
const message = ref('')
const router = useRouter()
const route = useRoute()


const loginForm = reactive({
  email: '',
  password: '',
  remember: false,
})

const signupForm = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const isEmail = (value: string) => /\S+@\S+\.\S+/.test(value)

const loginValid = computed(() => isEmail(loginForm.email) && loginForm.password.length >= 8)
const signupValid = computed(
  () =>
    signupForm.name.trim().length >= 2 &&
    isEmail(signupForm.email) &&
    signupForm.password.length >= 8 &&
    signupForm.password === signupForm.confirmPassword,
)

const switchMode = (nextMode: AuthMode) => {
  mode.value = nextMode
  error.value = ''
  message.value = ''
}

type LoginRequest = {
  email: string
  password: string
  remember: boolean
}

type SignupRequest = {
  name: string
  email: string
  password: string
}

type ApiUser = {
  id?: string | number
  name?: string
  email?: string
}

type AuthResponse = {
  accessToken?: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  user?: ApiUser
  message?: string
  error?: string
}

const readErrorMessage = (data: AuthResponse, fallback: string) =>
  data.message || data.error || fallback

const normalizeSession = (data: AuthResponse, remember: boolean): AuthSession => {
  const expiresAt = data.expiresIn ? Date.now() + data.expiresIn * 1000 : undefined
  return {
    accessToken: data.accessToken,
    tokenType: data.tokenType,
    expiresAt,
    user: data.user,
    sessionCookie: !data.accessToken && remember !== undefined,
  }
}

const redirectAfterLogin = () => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  const target = redirect && redirect !== '/' ? redirect : '/dashboard'
  router.push(target)
}

const submitLogin = async () => {
  error.value = ''
  message.value = ''

  if (!loginValid.value) {
    error.value = '이메일과 8자 이상의 비밀번호를 입력해 주세요.'
    return
  }

  loading.value = true
  try {
    const response = await apiFetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm satisfies LoginRequest),
    })
    const data = (await response.json().catch(() => ({}))) as AuthResponse
    if (!response.ok) throw new Error(readErrorMessage(data, '로그인에 실패했어요.'))

    const session = normalizeSession(data, loginForm.remember)
    saveSession(session, loginForm.remember)
    message.value = '로그인 완료. 잠시만 기다려 주세요.'
    redirectAfterLogin()
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '로그인에 실패했어요.'
  } finally {
    loading.value = false
  }
}

const submitSignup = async () => {
  error.value = ''
  message.value = ''

  if (!signupValid.value) {
    error.value =
      '모든 항목을 정확히 입력해 주세요. 비밀번호는 8자 이상이며 동일해야 합니다.'
    return
  }

  loading.value = true
  try {
    const response = await apiFetch('/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: signupForm.name,
        email: signupForm.email,
        password: signupForm.password,
      } satisfies SignupRequest),
    })
    const data = (await response.json().catch(() => ({}))) as AuthResponse
    if (!response.ok) throw new Error(readErrorMessage(data, '회원가입에 실패했어요.'))

    if (data.accessToken || data.refreshToken) {
      const session = normalizeSession(data, true)
      saveSession(session, true)
      message.value = '회원가입과 로그인이 완료되었습니다.'
      redirectAfterLogin()
      return
    }

    message.value = data.message || '회원가입이 완료되었습니다. 로그인해 주세요.'
    switchMode('login')
    loginForm.email = signupForm.email
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '회원가입에 실패했어요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="layout">
    <section class="panel">
      <div class="brand">
        <div class="brand-mark">
          <span class="dot dot-a"></span>
          <span class="dot dot-b"></span>
        </div>
        <div>
          <p class="eyebrow">{{ mode === 'login' ? '로그인' : '회원가입' }}</p>
          <h1>프로젝트 접근</h1>
          <p class="sub">Spring Boot 연동을 위한 통합 인증 화면</p>
        </div>
      </div>

      <div class="tabs" role="tablist" aria-label="Auth mode">
        <button
          type="button"
          class="tab"
          :class="{ active: mode === 'login' }"
          @click="switchMode('login')"
        >
          로그인
        </button>
        <button
          type="button"
          class="tab"
          :class="{ active: mode === 'signup' }"
          @click="switchMode('signup')"
        >
          회원가입
        </button>
      </div>

      <form v-if="mode === 'login'" class="form" @submit.prevent="submitLogin">
        <label>
          <span>이메일</span>
          <input
            v-model.trim="loginForm.email"
            type="email"
            autocomplete="email"
            placeholder="you@example.com"
            :class="{ invalid: loginForm.email && !isEmail(loginForm.email) }"
            required
          />
        </label>

        <label>
          <span>비밀번호</span>
          <input
            v-model="loginForm.password"
            type="password"
            autocomplete="current-password"
            placeholder="8자 이상 입력"
            minlength="8"
            :class="{ invalid: loginForm.password && loginForm.password.length < 8 }"
            required
          />
        </label>

        <div class="form-row">
          <label class="remember">
            <input v-model="loginForm.remember" type="checkbox" />
            <span>로그인 유지</span>
          </label>
          <a href="#">비밀번호 찾기</a>
        </div>

        <button class="submit" type="submit" :disabled="loading || !loginValid">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <form v-else class="form" @submit.prevent="submitSignup">
        <label>
          <span>이름</span>
          <input
            v-model.trim="signupForm.name"
            type="text"
            autocomplete="name"
            placeholder="이름 입력"
            :class="{ invalid: signupForm.name && signupForm.name.trim().length < 2 }"
            required
          />
        </label>

        <label>
          <span>이메일</span>
          <input
            v-model.trim="signupForm.email"
            type="email"
            autocomplete="email"
            placeholder="you@example.com"
            :class="{ invalid: signupForm.email && !isEmail(signupForm.email) }"
            required
          />
        </label>

        <label>
          <span>비밀번호</span>
          <input
            v-model="signupForm.password"
            type="password"
            autocomplete="new-password"
            placeholder="8자 이상 입력"
            minlength="8"
            :class="{ invalid: signupForm.password && signupForm.password.length < 8 }"
            required
          />
        </label>

        <label>
          <span>비밀번호 확인</span>
          <input
            v-model="signupForm.confirmPassword"
            type="password"
            autocomplete="new-password"
            placeholder="비밀번호 재입력"
            :class="{ invalid: signupForm.confirmPassword && signupForm.confirmPassword !== signupForm.password }"
            required
          />
        </label>

        <button class="submit" type="submit" :disabled="loading || !signupValid">
          {{ loading ? '회원가입 중...' : '회원가입' }}
        </button>
      </form>

      <p v-if="error" class="feedback error">{{ error }}</p>
      <p v-if="message" class="feedback ok">{{ message }}</p>
      <p class="helper">
        `VITE_API_BASE`에 백엔드 URL을 설정하고 `/auth/login`, `/auth/signup`으로 연결하세요.
      </p>
    </section>

    <section class="side">
      <div class="glass">
        <h2>백엔드 계약</h2>
        <ul>
          <li>`POST /auth/login`에 email, password, remember 전달</li>
          <li>`POST /auth/signup`에 name, email, password 전달</li>
          <li>세션 인증: `credentials: 'include'` 유지</li>
          <li>JWT 인증: 토큰 저장 후 Authorization 헤더 첨부</li>
        </ul>
      </div>
    </section>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 540px) minmax(280px, 1fr);
  gap: 32px;
  padding: 64px clamp(24px, 4vw, 80px);
  color: #e8f1ff;
}

.panel {
  background: rgba(17, 24, 39, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 18px;
  padding: 32px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(10px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(145deg, #0ea5e9, #6366f1);
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
}

.dot {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot-a {
  background: #7dd3fc;
  top: 14px;
  left: 14px;
}

.dot-b {
  background: #c4b5fd;
  bottom: 14px;
  right: 14px;
}

.eyebrow {
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #7dd3fc;
  font-size: 12px;
  margin-bottom: 4px;
}

h1 {
  font-size: clamp(26px, 4vw, 32px);
  font-weight: 600;
  margin-bottom: 4px;
}

.sub {
  color: #cbd5e1;
  font-size: 14px;
}

.tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 20px;
}

.tab {
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.04);
  color: #dbeafe;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  font-weight: 600;
}

.tab.active {
  border-color: rgba(125, 211, 252, 0.8);
  background: rgba(125, 211, 252, 0.16);
  color: #d6f0ff;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: #dbeafe;
}

input[type='text'],
input[type='email'],
input[type='password'] {
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  padding: 12px 14px;
  color: #e5e7eb;
  outline: none;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

input:focus {
  border-color: #7dd3fc;
  background: rgba(125, 211, 252, 0.05);
}

.invalid {
  border-color: #fca5a5;
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #cbd5e1;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.submit {
  margin-top: 6px;
  width: 100%;
  border: none;
  border-radius: 12px;
  padding: 14px;
  font-weight: 600;
  font-size: 15px;
  background: linear-gradient(135deg, #38bdf8 0%, #6366f1 100%);
  color: #0b1223;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.35);
}

.submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.submit:not(:disabled):hover {
  transform: translateY(-1px);
}

.feedback {
  margin-top: 12px;
  font-size: 14px;
}

.feedback.error {
  color: #fecdd3;
}

.feedback.ok {
  color: #bbf7d0;
}

.helper {
  margin-top: 14px;
  font-size: 13px;
  color: #cbd5e1;
  line-height: 1.5;
}

.side {
  display: grid;
  align-items: center;
}

.glass {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 18px;
  padding: 26px;
  backdrop-filter: blur(10px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.28);
}

.glass h2 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #e2e8f0;
}

.glass ul {
  list-style: disc;
  padding-left: 18px;
  color: #cbd5e1;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
    padding: 48px clamp(18px, 6vw, 42px);
  }

  .side {
    order: -1;
  }
}

@media (max-width: 520px) {
  .panel,
  .glass {
    padding: 22px;
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
