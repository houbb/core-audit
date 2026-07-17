<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTimeline } from '@/api/audit'
import TimelineNode from '@/components/TimelineNode.vue'
import PillBadge from '@/components/PillBadge.vue'
import JsonViewer from '@/components/JsonViewer.vue'
import type { Timeline, AuditEvent } from '@/types/audit'

const route = useRoute()
const router = useRouter()
const timeline = ref<Timeline | null>(null)
const selectedEvent = ref<AuditEvent | null>(null)
const loading = ref(true)
const error = ref('')

const events = computed(() => timeline.value?.events ?? [])

async function load() {
  loading.value = true
  error.value = ''
  try {
    timeline.value = await getTimeline(route.params.id as string)
    // 默认选中第一个事件
    if (events.value.length > 0) {
      selectedEvent.value = events.value[0]
    }
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function selectEvent(event: AuditEvent) {
  selectedEvent.value = event
}

function formatTime(iso: string): string {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}

function isSelected(event: AuditEvent): boolean {
  return selectedEvent.value?.id === event.id
}

function durationDisplay(ms: number | undefined): string {
  if (!ms) return '-'
  if (ms < 1000) return ms + 'ms'
  if (ms < 60000) return (ms / 1000).toFixed(1) + 's'
  return (ms / 60000).toFixed(1) + 'min'
}

onMounted(load)
</script>

<template>
  <div class="timeline-page">
    <!-- Left Column: Timeline Info + related links -->
    <aside class="col-left">
      <div class="page-header" style="margin-bottom: 16px;">
        <h1 class="page-title">Timeline</h1>
        <p class="page-subtitle">{{ timeline?.title || '加载中…' }}</p>
      </div>

      <div v-if="timeline" class="card">
        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">类型</span>
            <span class="detail-value"><PillBadge type="module" :value="timeline.type" /></span>
          </div>
          <div class="detail-field">
            <span class="detail-label">事件数</span>
            <span class="detail-value">{{ events.length }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">耗时</span>
            <span class="detail-value">{{ durationDisplay(timeline.duration) }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">开始</span>
            <span class="detail-value">{{ formatTime(timeline.startTime) }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">结束</span>
            <span class="detail-value">{{ formatTime(timeline.endTime) }}</span>
          </div>
        </div>

        <!-- Quick links -->
        <div style="margin-top: 16px; display: flex; flex-wrap: wrap; gap: 8px;">
          <button
            v-for="ev in events.slice(0, 1)"
            :key="ev.operatorId"
            v-if="ev.operatorId"
            class="btn"
            @click="router.push(`/operator/${ev.operatorId}/timeline`)"
          >
            <FontAwesomeIcon icon="user" /> {{ ev.operatorName || ev.operatorId }}
          </button>
          <button
            v-for="ev in events.slice(0, 1)"
            :key="ev.traceId"
            v-if="ev.traceId"
            class="btn"
            @click="router.push(`/trace/${ev.traceId}`)"
          >
            <FontAwesomeIcon icon="link" /> Trace
          </button>
          <button
            v-for="ev in events.slice(0, 1)"
            :key="ev.targetId"
            v-if="ev.targetId"
            class="btn"
            @click="router.push(`/object/${ev.targetId}/timeline`)"
          >
            <FontAwesomeIcon icon="box" /> 对象
          </button>
        </div>
      </div>

      <div v-if="loading" class="empty-state" style="margin-top: 16px;">
        <div class="empty-state-text">加载中…</div>
      </div>
      <div v-if="error" class="empty-state" style="margin-top: 16px;">
        <div class="empty-state-icon"><FontAwesomeIcon icon="exclamation-triangle" /></div>
        <div class="empty-state-text">{{ error }}</div>
      </div>
    </aside>

    <!-- Center Column: Time Axis -->
    <section class="col-center">
      <div v-if="events.length > 0" class="time-axis">
        <TimelineNode
          v-for="(ev, idx) in events"
          :key="ev.id"
          :event="ev"
          :is-selected="isSelected(ev)"
          :is-first="idx === 0"
          :is-last="idx === events.length - 1"
          @select="selectEvent"
        />
      </div>
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-state-icon"><FontAwesomeIcon icon="clock" /></div>
        <div class="empty-state-text">暂无事件</div>
      </div>
    </section>

    <!-- Right Column: Event Detail -->
    <section class="col-right">
      <div v-if="selectedEvent" class="card">
        <div class="page-header" style="margin-bottom: 16px;">
          <h2 style="font-size: 14px; font-weight: 600;">事件详情</h2>
        </div>

        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">时间</span>
            <span class="detail-value">{{ formatTime(selectedEvent.createdAt) }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">模块</span>
            <span class="detail-value"><PillBadge type="module" :value="selectedEvent.module" /></span>
          </div>
          <div class="detail-field">
            <span class="detail-label">操作</span>
            <span class="detail-value"><PillBadge type="action" :value="selectedEvent.action" /></span>
          </div>
          <div class="detail-field">
            <span class="detail-label">结果</span>
            <span class="detail-value">
              <PillBadge
                :type="selectedEvent.result === 'SUCCESS' ? 'success' : 'fail'"
                :value="selectedEvent.result"
              />
            </span>
          </div>
          <div class="detail-field">
            <span class="detail-label">对象</span>
            <span class="detail-value">{{ selectedEvent.targetType }}#{{ selectedEvent.targetId }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">操作人</span>
            <span class="detail-value">{{ selectedEvent.operatorName || selectedEvent.operatorId || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">IP</span>
            <span class="detail-value text-mono">{{ selectedEvent.clientIp || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">URI</span>
            <span class="detail-value text-mono">{{ selectedEvent.requestUri || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">Trace ID</span>
            <span class="detail-value text-mono">{{ selectedEvent.traceId || '-' }}</span>
          </div>
          <div class="detail-field" style="grid-column: 1 / -1;">
            <span class="detail-label">描述</span>
            <span class="detail-value">{{ selectedEvent.description || '-' }}</span>
          </div>
        </div>

        <div style="margin-top: 16px;">
          <JsonViewer :data="selectedEvent.metadata" />
        </div>
      </div>
      <div v-else class="empty-state" style="margin-top: 16px;">
        <div class="empty-state-text">点击左侧事件查看详情</div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.timeline-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
  padding: 24px;
  gap: 24px;
}

/* ---- Left Column ---- */
.col-left {
  width: 280px;
  flex-shrink: 0;
  overflow-y: auto;
}

/* ---- Center Column (Time Axis) ---- */
.col-center {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}

.time-axis {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ---- Right Column (Event Detail) ---- */
.col-right {
  width: 340px;
  flex-shrink: 0;
  overflow-y: auto;
}
</style>