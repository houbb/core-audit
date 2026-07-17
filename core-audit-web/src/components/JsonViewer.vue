<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  data: Record<string, unknown> | null
}>()

const collapsed = ref(true)

function toggle() {
  collapsed.value = !collapsed.value
}

function formatJson(obj: Record<string, unknown> | null): string {
  if (!obj || Object.keys(obj).length === 0) return '{}'
  return JSON.stringify(obj, null, 2)
}
</script>

<template>
  <div class="json-viewer">
    <div class="json-header" @click="toggle">
      <span class="json-toggle">{{ collapsed ? '▸' : '▾' }}</span>
      <span class="json-title">Metadata</span>
    </div>
    <div v-if="!collapsed" class="json-block">{{ formatJson(data) }}</div>
  </div>
</template>

<style scoped>
.json-viewer {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.json-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--bg-secondary);
  cursor: pointer;
  user-select: none;
}

.json-toggle {
  font-size: 12px;
  color: var(--text-secondary);
  width: 14px;
}

.json-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
