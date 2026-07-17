<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/audit'
import StatCard from '@/components/StatCard.vue'
import AuditTable from '@/components/AuditTable.vue'
import type { DashboardStats } from '@/types/audit'

const router = useRouter()
const stats = ref<DashboardStats | null>(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
}

function formatDecimal(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

function durationDisplay(ms: number): string {
  if (!ms || ms === 0) return '-'
  if (ms < 1000) return Math.round(ms) + 'ms'
  return (ms / 1000).toFixed(1) + 's'
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">审计概览</h1>
      <p class="page-subtitle">Core Platform 审计运行时 Dashboard</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <StatCard label="今日审计" :value="stats?.todayTotal ?? 0" icon="chart-bar" />
      <StatCard label="成功" :value="stats?.todaySuccess ?? 0" icon="check" />
      <StatCard label="失败" :value="stats?.todayFail ?? 0" icon="times" />
      <StatCard label="活跃模块" :value="stats?.activeModules ?? 0" icon="table-cells" />
    </div>

    <!-- P5 Timeline 统计 -->
    <div class="stats-grid" style="margin-top: 16px;">
      <StatCard label="今日 Timeline" :value="stats?.todayTimelineCount ?? 0" icon="clock" />
      <StatCard label="平均事件数" :value="formatDecimal(stats?.avgTimelineLength ?? 0)" icon="equals" />
      <StatCard label="最长事件链" :value="stats?.maxTimelineLength ?? 0" icon="arrow-up" />
      <StatCard label="平均耗时" :value="durationDisplay(stats?.avgTimelineDuration ?? 0)" icon="stopwatch" />
    </div>

    <!-- 最近操作 -->
    <div class="card" style="margin-top: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h2 style="font-size: 14px; font-weight: 600;">最近操作</h2>
        <button class="btn" @click="router.push('/events')">查看全部 →</button>
      </div>
      <AuditTable :events="stats?.recentEvents ?? []" compact />
    </div>
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>
