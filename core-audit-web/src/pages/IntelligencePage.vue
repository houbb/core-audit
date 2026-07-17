<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getIntelligenceDashboard, queryInsights, getRecommendations } from '@/api/intelligence'
import StatCard from '@/components/StatCard.vue'
import PillBadge from '@/components/PillBadge.vue'
import type { IntelligenceDashboard, AuditInsight } from '@/types/audit'

const router = useRouter()
const stats = ref<IntelligenceDashboard | null>(null)
const insights = ref<AuditInsight[]>([])
const recommendations = ref<AuditInsight[]>([])
const loading = ref(true)

function severityVariant(severity: string): 'success' | 'fail' | 'neutral' {
  switch (severity) {
    case 'CRITICAL': return 'fail'
    case 'HIGH': return 'fail'
    case 'MEDIUM': return 'neutral'
    default: return 'success'
  }
}

function formatScore(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

async function load() {
  loading.value = true
  try {
    const [d, i, r] = await Promise.all([
      getIntelligenceDashboard(),
      queryInsights(1, 20),
      getRecommendations(10),
    ])
    stats.value = d
    insights.value = i
    recommendations.value = r
  } finally {
    loading.value = false
  }
}

function viewInsight(id: string) {
  router.push(`/intelligence/insights/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">Audit Intelligence</h1>
      <p class="page-subtitle">AI 驱动的智能分析、风险评估与安全建议</p>
    </div>

    <!-- Risk 统计卡片 -->
    <div class="stats-grid">
      <StatCard label="今日 Insight" :value="stats?.todayInsightCount ?? 0" icon="lightbulb" />
      <StatCard label="Critical" :value="stats?.todayCriticalCount ?? 0" icon="exclamation-triangle" />
      <StatCard label="High" :value="stats?.todayHighCount ?? 0" icon="arrow-up" />
      <StatCard label="Avg Risk Score" :value="formatScore(stats?.avgRiskScore ?? 0)" icon="chart-line" />
    </div>

    <!-- 三栏布局 -->
    <div class="three-col">
      <!-- 左栏：Insight 列表 -->
      <div class="col">
        <div class="card" style="height: 100%;">
          <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 16px;">
            <FontAwesomeIcon icon="lightbulb" style="margin-right: 8px;" />Insights
          </h2>
          <div v-if="insights.length === 0" class="empty-state">
            <div class="empty-state-text">暂无 Insight</div>
          </div>
          <div v-else class="insight-list">
            <div
              v-for="item in insights"
              :key="item.id"
              class="insight-item"
              @click="viewInsight(item.id)"
            >
              <div class="insight-header">
                <PillBadge :type="severityVariant(item.severity)" :value="item.severity" />
                <span class="insight-title">{{ item.title }}</span>
              </div>
              <div class="insight-summary">{{ item.summary }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中栏：Recommendation -->
      <div class="col">
        <div class="card" style="height: 100%;">
          <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 16px;">
            <FontAwesomeIcon icon="shield-halved" style="margin-right: 8px;" />Recommendations
          </h2>
          <div v-if="recommendations.length === 0" class="empty-state">
            <div class="empty-state-text">暂无建议</div>
          </div>
          <div v-else class="rec-list">
            <div v-for="item in recommendations" :key="item.id" class="rec-item">
              <FontAwesomeIcon icon="check-circle" class="rec-icon" />
              <div>
                <div class="rec-title">{{ item.title }}</div>
                <div class="rec-text">{{ item.suggestion || item.summary }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏：Risk Overview -->
      <div class="col">
        <div class="card" style="height: 100%;">
          <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 16px;">
            <FontAwesomeIcon icon="shield" style="margin-right: 8px;" />Risk Overview
          </h2>
          <div class="risk-overview">
            <div class="risk-row">
              <span class="risk-label">Critical</span>
              <span class="risk-value critical">{{ stats?.todayCriticalCount ?? 0 }}</span>
            </div>
            <div class="risk-row">
              <span class="risk-label">High</span>
              <span class="risk-value high">{{ stats?.todayHighCount ?? 0 }}</span>
            </div>
            <div class="risk-row">
              <span class="risk-label">Medium</span>
              <span class="risk-value medium">{{ stats?.todayMediumCount ?? 0 }}</span>
            </div>
            <div class="risk-row" style="margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border);">
              <span class="risk-label">Avg Score</span>
              <span class="risk-value">{{ formatScore(stats?.avgRiskScore ?? 0) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.three-col {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  margin-top: 24px;
}

@media (max-width: 1200px) {
  .three-col {
    grid-template-columns: 1fr;
  }
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.insight-item {
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.15s;
}

.insight-item:hover {
  background: var(--bg-secondary);
}

.insight-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.insight-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.insight-summary {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rec-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rec-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.rec-icon {
  color: var(--accent);
  font-size: 14px;
  margin-top: 2px;
  flex-shrink: 0;
}

.rec-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.rec-text {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.risk-overview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.risk-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.risk-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.risk-value.critical {
  color: var(--fail-text);
}

.risk-value.high {
  color: #e67e22;
}

.risk-value.medium {
  color: #f1c40f;
}
</style>