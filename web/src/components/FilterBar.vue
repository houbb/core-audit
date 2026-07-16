<script setup lang="ts">
import { ref, reactive } from 'vue'
import { exportAuditEvents } from '@/api/audit'
import type { AuditQuery } from '@/types/audit'

const emit = defineEmits<{
  (e: 'search', query: AuditQuery): void
  (e: 'reset'): void
}>()

const filters = reactive<AuditQuery>({
  page: 1,
  size: 20,
  module: '',
  action: '',
  result: '',
  keyword: '',
  startTime: '',
  endTime: '',
})

const timePreset = ref('all')

function setTimePreset(val: string) {
  timePreset.value = val
  const now = new Date()
  const fmt = (d: Date) => d.toISOString().slice(0, 19)

  switch (val) {
    case 'today':
      filters.startTime = fmt(new Date(now.getFullYear(), now.getMonth(), now.getDate()))
      filters.endTime = ''
      break
    case '7d':
      const d7 = new Date(now.getTime() - 7 * 86400000)
      filters.startTime = fmt(d7)
      filters.endTime = ''
      break
    case '30d':
      const d30 = new Date(now.getTime() - 30 * 86400000)
      filters.startTime = fmt(d30)
      filters.endTime = ''
      break
    default:
      filters.startTime = ''
      filters.endTime = ''
  }
}

function search() {
  emit('search', { ...filters, page: 1 })
}

function reset() {
  filters.module = ''
  filters.action = ''
  filters.result = ''
  filters.keyword = ''
  filters.startTime = ''
  filters.endTime = ''
  timePreset.value = 'all'
  emit('reset')
}

function handleExport() {
  exportAuditEvents(filters)
}
</script>

<template>
  <div class="filter-bar">
    <div class="filter-row">
      <select class="form-select" v-model="timePreset" @change="setTimePreset(($event.target as HTMLSelectElement).value)">
        <option value="all">全部时间</option>
        <option value="today">今天</option>
        <option value="7d">近 7 天</option>
        <option value="30d">近 30 天</option>
      </select>

      <select class="form-select" v-model="filters.module">
        <option value="">全部模块</option>
        <option value="USER">USER</option>
        <option value="CONFIG">CONFIG</option>
        <option value="AI">AI</option>
        <option value="FILE">FILE</option>
        <option value="STORAGE">STORAGE</option>
        <option value="WORKFLOW">WORKFLOW</option>
        <option value="OPENAPI">OPENAPI</option>
        <option value="NOTIFICATION">NOTIFICATION</option>
        <option value="BILLING">BILLING</option>
      </select>

      <select class="form-select" v-model="filters.action">
        <option value="">全部操作</option>
        <option value="CREATE">CREATE</option>
        <option value="UPDATE">UPDATE</option>
        <option value="DELETE">DELETE</option>
        <option value="LOGIN">LOGIN</option>
        <option value="LOGOUT">LOGOUT</option>
        <option value="UPLOAD">UPLOAD</option>
        <option value="DOWNLOAD">DOWNLOAD</option>
        <option value="EXPORT">EXPORT</option>
        <option value="IMPORT">IMPORT</option>
        <option value="ENABLE">ENABLE</option>
        <option value="DISABLE">DISABLE</option>
        <option value="APPROVE">APPROVE</option>
        <option value="REJECT">REJECT</option>
        <option value="EXECUTE">EXECUTE</option>
        <option value="CALL">CALL</option>
      </select>

      <select class="form-select" v-model="filters.result">
        <option value="">全部结果</option>
        <option value="SUCCESS">SUCCESS</option>
        <option value="FAIL">FAIL</option>
      </select>

      <input class="form-input" v-model="filters.keyword" placeholder="关键字搜索…" @keyup.enter="search" />
    </div>

    <div class="filter-actions">
      <button class="btn btn-primary" @click="search">查询</button>
      <button class="btn" @click="reset">重置</button>
      <button class="btn" @click="handleExport">导出 CSV</button>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding: 16px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}
</style>
