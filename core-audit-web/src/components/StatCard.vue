<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  label: string
  value: number | string
  icon?: string
}>()
</script>

<template>
  <div class="stat-card">
    <div class="stat-icon">
      <FontAwesomeIcon :icon="icon || 'chart-bar'" />
    </div>
    <div class="stat-body">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  transition: box-shadow 0.15s;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
}

.stat-icon {
  font-size: 20px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  flex-shrink: 0;
  color: var(--accent);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}
</style>