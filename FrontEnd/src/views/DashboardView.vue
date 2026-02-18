<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '../utils/api'
import { clearSession, loadSession } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const message = ref('')

const session = computed(() => loadSession())
const userName = computed(() => session.value?.user?.name ?? '사용자')
const userEmail = computed(() => session.value?.user?.email ?? '-')

const submitLogout = async () => {
  error.value = ''
  message.value = ''
  loading.value = true
  try {
    await apiFetch('/auth/logout', { method: 'POST' })
  } catch {
    // Logout failure shouldn't block local sign-out.
  } finally {
    clearSession()
    loading.value = false
    message.value = '로그아웃되었습니다.'
    router.replace('/')
  }
}
</script>

<template>
  <div class="dashboard">
    <section class="card">
      <div class="header">
        <div>
          <p class="eyebrow">인증 완료</p>
          <h1>대시보드</h1>
          <p class="sub">로그인 이후 접근 가능한 보호 영역입니다.</p>
        </div>
        <button class="logout" type="button" :disabled="loading" @click="submitLogout">
          {{ loading ? '로그아웃 중...' : '로그아웃' }}
        </button>
      </div>

      <div class="profile">
        <div class="badge">
          <span>{{ userName.slice(0, 1) }}</span>
        </div>
        <div>
          <p class="name">{{ userName }}</p>
          <p class="email">{{ userEmail }}</p>
        </div>
      </div>

      <p v-if="error" class="feedback error">{{ error }}</p>
      <p v-if="message" class="feedback ok">{{ message }}</p>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 48px 20px;
  color: #e5ecf5;
}

.card {
  width: min(640px, 100%);
  background: rgba(17, 24, 39, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(12px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.eyebrow {
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #7dd3fc;
  font-size: 12px;
  margin-bottom: 6px;
}

h1 {
  font-size: clamp(28px, 5vw, 36px);
  margin-bottom: 6px;
}

.sub {
  color: #cbd5e1;
  font-size: 14px;
}

.logout {
  border: none;
  border-radius: 12px;
  padding: 10px 16px;
  background: rgba(248, 113, 113, 0.16);
  color: #fecaca;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.logout:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.logout:not(:disabled):hover {
  background: rgba(248, 113, 113, 0.25);
  color: #fee2e2;
}

.profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 26px;
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.badge {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(145deg, #38bdf8, #6366f1);
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #0b1223;
  font-size: 20px;
}

.name {
  font-size: 18px;
  font-weight: 600;
}

.email {
  color: #cbd5e1;
  font-size: 14px;
}

.feedback {
  margin-top: 14px;
  font-size: 14px;
}

.feedback.error {
  color: #fecdd3;
}

.feedback.ok {
  color: #bbf7d0;
}

@media (max-width: 520px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .card {
    padding: 24px;
  }
}
</style>
