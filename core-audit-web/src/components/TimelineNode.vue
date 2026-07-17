<script setup lang="ts">
import type { AuditEvent } from '@/types/audit'

defineProps<{
  event: AuditEvent
  isSelected: boolean
  isFirst: boolean
  isLast: boolean
}>()

defineEmits<{
  select: [event: AuditEvent]
}>()

function formatTime(iso: string): string {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(11, 19)
}

function shortAction(action: string): string {
  const map: Record<string, string> = {
    CREATE: '创建', UPDATE: '更新', DELETE: '删除',
    LOGIN: '登录', LOGOUT: '登出', UPLOAD: '上传', DOWNLOAD: '下载',
    EXPORT: '导出', IMPORT: '导入', ENABLE: '启用', DISABLE: '禁用',
    APPROVE: '审批', REJECT: '拒绝', EXECUTE: '执行', CALL: '调用'
  }
  return map[action] || action
}
</script>

<template>
  <div class="timeline-node" :class="{ selected: isSelected }" @click="$emit('select', event)">
    <!-- 时间 -->
    <div class="node-time">{{ formatTime(event.createdAt) }}</div>
    <!-- 连线 + 圆点 -->
    <div class="node-track">
      <div class="track-line top" :class="{ hidden: isFirst }" />
      <div class="track-dot" :class="event.result === 'FAIL' ? 'fail' : ''" />
      <div class="track-line bottom" :class="{ hidden: isLast }" />
    </div>
    <!-- 事件摘要 -->
    <div class="node-content">
      <span class="node-module">{{ event.module }}</span>
      <span class="node-action">{{ shortAction(event.action) }}</span>
      <span class="node-target">{{ event.targetType }}#{{ event.targetId }}</span>
    </div>
  </div>
</template>

<style scoped>
.timeline-node {
  display: flex;
  align-items: stretch;
  gap: 12px;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background 0.15s;
  min-height: 48px;
}

.timeline-node:hover {
  background: var(--bg-secondary);
}

.timeline-node.selected {
  background: rgba(0, 113, 227, 0.08);
  border-left: 3px solid var(--accent);
  padding-left: 9px;
}

/* ---- Time ---- */
.node-time {
  width: 56px;
  font-size: 11px;
  font-family: var(--font-family);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* ---- Track (vertical line + dot) ---- */
.node-track {
  width: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.track-line {
  width: 2px;
  flex: 1;
  background: var(--border);
  min-height: 8px;
}

.track-line.hidden {
  visibility: hidden;
}

.track-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--accent);
  flex-shrink: 0;
}

.track-dot.fail {
  background: var(--fail-text);
  border-color: var(--fail-text);
}

/* ---- Content ---- */
.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  font-size: 13px;
  overflow: hidden;
}

.node-module {
  font-weight: 600;
  color: var(--accent);
  flex-shrink: 0;
}

.node-action {
  color: var(--text-primary);
  flex-shrink: 0;
}

.node-target {
  color: var(--text-secondary);
  font-family: var(--font-family);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>