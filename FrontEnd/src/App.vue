<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="page">
    <RouterView />
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: radial-gradient(circle at 20% 20%, #1b2a4a 0, #0f172a 35%, #0b1223 70%),
    linear-gradient(135deg, #0ea5e9 0%, #6366f1 50%, #0ea5e9 100%);
  color: #e5ecf5;
}
</style>
