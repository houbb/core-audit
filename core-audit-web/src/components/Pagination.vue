<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  total: number
  hasNext: boolean
}>()

const emit = defineEmits<{
  (e: 'change', page: number): void
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / 20)))
</script>

<template>
  <div class="pagination">
    <button class="btn" :disabled="page <= 1" @click="emit('change', page - 1)">← 上一页</button>
    <span class="page-info">{{ page }} / {{ totalPages }} 页 (共 {{ total }} 条)</span>
    <button class="btn" :disabled="!hasNext" @click="emit('change', page + 1)">下一页 →</button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 0;
}

.page-info {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
