<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEnterpriseOverview, getEnterpriseHealth } from '@/api/enterprise'
import StatCard from '@/components/StatCard.vue'

const router = useRouter()

interface OverviewData {
  auditEvents: { today: number; success: number; fail: number; activeModules: number }
  highRisk: { critical: number; high: number; avgRiskScore: number }
  timeline: { todayCount: number; avgLength: number; maxLength: number }
  compliance: { hashVerifyRate: number; legalHoldCount: number; retentionPolicyCount: number }
  enterprise: { sourceCount: number; providerCount: number; subscriptionCount: number; webhookDeliveryCount: number }
  recentEvents: Array<Record<string, unknown>>
}

const overview = ref<OverviewData | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    overview.value = await getEnterpriseOverview()
  } catch (e) {
    console.error('Failed to load enterprise overview', e)
  } finally {
    loading.value = false
  }
})

function formatDecimal(v: number | undefined): string {
  if (v === undefined || v === null) return '0'
  return Number.isInteger(v) ? v.toString() : v.toFixed(1)
}
</script>

<template>
  <div class="enterprise-dashboard">
    <div class="page-header">
      <div>
        <h1>Enterprise Audit Platform</h1>
        <p class="subtitle">企业统一审计运营中心</p>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-else-if="overview">
      <!-- Row 1: Core Stats -->
      <section class="stat-grid">
        <StatCard icon="database" label="今日审计事件" :value="overview.auditEvents.today" />
        <StatCard icon="circle-exclamation" label="高风险事件" :value="overview.highRisk.critical + overview.highRisk.high" />
        <StatCard icon="clock" label="今日 Timeline" :value="overview.timeline.todayCount" />
        <StatCard icon="shield" label="合规状态" :value="overview.compliance.hashVerifyRate + '%'" />
      </section>

      <!-- Row 2: Enterprise Stats -->
      <section class="stat-grid">
        <StatCard icon="server" label="接入来源" :value="overview.enterprise.sourceCount" />
        <StatCard icon="puzzle-piece" label="已装插件" :value="overview.enterprise.providerCount" />
        <StatCard icon="bell" label="Webhook 订阅" :value="overview.enterprise.subscriptionCount" />
        <StatCard icon="paper-plane" label="今日投递" :value="overview.enterprise.webhookDeliveryCount" />
      </section>

      <!-- Row 3: Quick Access Cards -->
      <section class="quick-actions">
        <div class="action-card" @click="router.push('/enterprise/sources')">
          <span class="action-icon"><FontAwesomeIcon icon="server" /></span>
          <span class="action-label">Sources</span>
          <span class="action-desc">来源系统管理</span>
        </div>
        <div class="action-card" @click="router.push('/enterprise/providers')">
          <span class="action-icon"><FontAwesomeIcon icon="puzzle-piece" /></span>
          <span class="action-label">Marketplace</span>
          <span class="action-desc">插件市场</span>
        </div>
        <div class="action-card" @click="router.push('/enterprise/subscriptions')">
          <span class="action-icon"><FontAwesomeIcon icon="bell" /></span>
          <span class="action-label">Webhooks</span>
          <span class="action-desc">通知订阅管理</span>
        </div>
        <div class="action-card" @click="router.push('/enterprise/search')">
          <span class="action-icon"><FontAwesomeIcon icon="magnifying-glass" /></span>
          <span class="action-label">Global Search</span>
          <span class="action-desc">企业全局搜索</span>
        </div>
      </section>

      <!-- Row 4: Intelligence & Compliance -->
      <section class="info-grid">
        <div class="info-panel">
          <h3><FontAwesomeIcon icon="chart-bar" /> Intelligence</h3>
          <div class="info-row">
            <span>严重风险</span>
            <span class="badge critical">{{ overview.highRisk.critical }}</span>
          </div>
          <div class="info-row">
            <span>高风险</span>
            <span class="badge high">{{ overview.highRisk.high }}</span>
          </div>
          <div class="info-row">
            <span>平均风险评分</span>
            <span>{{ formatDecimal(overview.highRisk.avgRiskScore) }}</span>
          </div>
        </div>
        <div class="info-panel">
          <h3><FontAwesomeIcon icon="shield" /> Compliance</h3>
          <div class="info-row">
            <span>哈希验证率</span>
            <span>{{ formatDecimal(overview.compliance.hashVerifyRate) }}%</span>
          </div>
          <div class="info-row">
            <span>法律保留</span>
            <span>{{ overview.compliance.legalHoldCount }}</span>
          </div>
          <div class="info-row">
            <span>保留策略</span>
            <span>{{ overview.compliance.retentionPolicyCount }}</span>
          </div>
        </div>
        <div class="info-panel">
          <h3><FontAwesomeIcon icon="clock" /> Timeline</h3>
          <div class="info-row">
            <span>今日时间线</span>
            <span>{{ overview.timeline.todayCount }}</span>
          </div>
          <div class="info-row">
            <span>平均事件数</span>
            <span>{{ formatDecimal(overview.timeline.avgLength) }}</span>
          </div>
          <div class="info-row">
            <span>最长事件链</span>
            <span>{{ overview.timeline.maxLength }}</span>
          </div>
        </div>
      </section>

      <!-- Recent Events -->
      <section class="recent-section" v-if="overview.recentEvents?.length">
        <h3>最近审计事件</h3>
        <div class="event-list">
          <div v-for="(event, idx) in overview.recentEvents" :key="idx" class="event-row">
            <span class="pill" :class="(event as any).result === 'SUCCESS' ? 'success' : 'fail'">
              {{ (event as any).result }}
            </span>
            <span class="event-module">{{ (event as any).module }}</span>
            <span class="event-desc">{{ (event as any).description }}</span>
            <span class="event-time">{{ (event as any).createdAt }}</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.enterprise-dashboard {
  padding: 32px;
  max-width: 1400px;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0 0;
}

.loading {
  text-align: center;
  padding: 60px;
  color: var(--text-secondary);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.action-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
}

.action-card:hover {
  border-color: var(--accent);
  background: var(--accent-bg);
}

.action-icon {
  font-size: 28px;
  display: block;
  margin-bottom: 8px;
}

.action-label {
  font-size: 14px;
  font-weight: 600;
  display: block;
}

.action-desc {
  font-size: 11px;
  color: var(--text-secondary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.info-panel {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 20px;
}

.info-panel h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.badge {
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.badge.critical {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.badge.high {
  background: rgba(234, 179, 8, 0.1);
  color: #ca8a04;
}

.recent-section {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 20px;
}

.recent-section h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}

.event-row:last-child {
  border-bottom: none;
}

.pill {
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.pill.success {
  background: rgba(22, 163, 74, 0.1);
  color: #16a34a;
}

.pill.fail {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.event-module {
  font-weight: 600;
  color: var(--accent);
  min-width: 80px;
}

.event-desc {
  flex: 1;
  color: var(--text-primary);
}

.event-time {
  color: var(--text-secondary);
  font-size: 11px;
  white-space: nowrap;
}
</style>