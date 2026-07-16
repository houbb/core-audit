<script setup lang="ts">
import { useRouter } from 'vue-router'
import PillBadge from './PillBadge.vue'
import type { AuditEvent } from '@/types/audit'

defineProps<{
  events: AuditEvent[]
  compact?: boolean
}>()

const router = useRouter()

function goDetail(id: string) {
  router.push(`/events/${id}`)
}

function formatTime(iso: string): string {
  if (!iso) return ''
  return iso.replace('T', ' ').substring(0, 19)
}
</script>

<template>
  <div class="table-wrapper">
    <table>
      <thead>
        <tr>
          <th>时间</th>
          <th>模块</th>
          <th>操作</th>
          <th>对象</th>
          <th>操作人</th>
          <th>结果</th>
          <th>描述</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="events.length === 0">
          <td :colspan="8">
            <div class="empty-state">
              <div class="empty-state-icon">📭</div>
              <div class="empty-state-text">暂无审计记录</div>
            </div>
          </td>
        </tr>
        <tr v-for="event in events" :key="event.id" @click="goDetail(event.id)" style="cursor: pointer;">
          <td class="text-mono">{{ formatTime(event.createdAt) }}</td>
          <td><PillBadge type="module" :value="event.module" /></td>
          <td><PillBadge type="action" :value="event.action" /></td>
          <td>{{ event.targetType }}:{{ event.targetId || '-' }}</td>
          <td>{{ event.operatorName || '-' }}</td>
          <td>
            <PillBadge
              :type="event.result === 'SUCCESS' ? 'success' : 'fail'"
              :value="event.result"
            />
          </td>
          <td class="desc-cell">{{ event.description || '-' }}</td>
          <td><span class="arrow">›</span></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.desc-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow {
  font-size: 16px;
  color: var(--text-tertiary);
  font-weight: 300;
}

tr:hover .arrow {
  color: var(--accent);
}
</style>
