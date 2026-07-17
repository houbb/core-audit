<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getObjectTimeline, getOperatorTimeline, getTraceTimeline } from '@/api/audit'
import type { Timeline } from '@/types/audit'

const route = useRoute()
const router = useRouter()
const timelines = ref<Timeline[]>([])
const loading = ref(true)
const error = ref('')

const routeName = computed(() => route.name as string)
const paramId = computed(() => route.params.id as string || route.params.traceId as string)

const pageTitle = computed(() => {
  const map: Record<string, string> = {
    'object-timeline': '对象时间线 — ' + paramId.value,
    'operator-timeline': '操作人时间线 — ' + paramId.value,
    'trace-timeline': '请求链路 — ' + paramId.value,
  }
  return map[routeName.value] || 'Timeline'
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const name = routeName.value
    if (name === 'object-timeline') {
      timelines.value = await getObjectTimeline(paramId.value)
    } else if (name === 'operator-timeline') {
      timelines.value = await getOperatorTimeline(paramId.value)
    } else if (name === 'trace-timeline') {
      timelines.value = await getTraceTimeline(paramId.value)
    }
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function formatTime(iso: string): string {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}

function eventCount(t: Timeline): number {
  return t.events?.length ?? 0
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <p class="page-subtitle">共 {{ timelines.length }} 条时间线</p>
    </div>

    <div v-if="loading" class="empty-state">
      <div class="empty-state-text">加载中…</div>
    </div>

    <div v-else-if="error" class="empty-state">
      <div class="empty-state-icon"><FontAwesomeIcon icon="exclamation-triangle" /></div>
      <div class="empty-state-text">{{ error }}</div>
    </div>

    <template v-else-if="timelines.length > 0">
      <div class="timeline-list">
        <div
          v-for="t in timelines"
          :key="t.id"
          class="card timeline-card"
          @click="router.push(`/timeline/${t.id}`)"
        >
          <div class="tl-header">
            <span class="tl-type">{{ t.type }}</span>
            <span class="tl-count">{{ eventCount(t) }} 事件</span>
          </div>
          <div class="tl-title">{{ t.title }}</div>
          <div class="tl-meta">
            <span>{{ formatTime(t.startTime) }} → {{ formatTime(t.endTime) }}</span>
            <span v-if="t.duration" style="margin-left: 12px;">{{ t.duration }}ms</span>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-state-icon"><FontAwesomeIcon icon="clock" /></div>
      <div class="empty-state-text">暂无相关时间线</div>
    </div>
  </div>
</template>

<style scoped>
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-card {
  cursor: pointer;
  transition: box-shadow 0.15s;
}

.timeline-card:hover {
  box-shadow: var(--shadow-md);
}

.tl-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tl-type {
  font-size: 11px;
  font-weight: 600;
  color: var(--accent);
  background: rgba(0, 113, 227, 0.08);
  padding: 2px 8px;
  border-radius: var(--radius-pill);
}

.tl-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.tl-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.tl-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>