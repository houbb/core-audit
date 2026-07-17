<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInsight, getRisk } from '@/api/intelligence'
import PillBadge from '@/components/PillBadge.vue'
import type { AuditInsight, AuditRisk } from '@/types/audit'

const route = useRoute()
const router = useRouter()
const insight = ref<AuditInsight | null>(null)
const risk = ref<AuditRisk | null>(null)
const loading = ref(true)

function severityVariant(severity: string): 'success' | 'fail' | 'neutral' {
  switch (severity) {
    case 'CRITICAL': return 'fail'
    case 'HIGH': return 'fail'
    case 'MEDIUM': return 'neutral'
    default: return 'success'
  }
}

function formatEvidence(json: string): Record<string, unknown> | null {
  if (!json) return null
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

async function load() {
  loading.value = true
  try {
    const id = route.params.id as string
    insight.value = await getInsight(id)

    if (insight.value?.auditId) {
      try {
        risk.value = await getRisk(insight.value.auditId)
      } catch {
        risk.value = null
      }
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/intelligence')
}

onMounted(load)
</script>

<template>
  <div class="page">
    <!-- Breadcrumb -->
    <div class="breadcrumb">
      <a href="#" @click.prevent="goBack">Intelligence</a>
      <span class="sep">/</span>
      <span>Insight Detail</span>
    </div>

    <div v-if="loading" class="empty-state">
      <div class="empty-state-text">加载中…</div>
    </div>

    <template v-else-if="insight">
      <div class="page-header">
        <div style="display: flex; align-items: center; gap: 12px;">
          <h1 class="page-title">{{ insight.title }}</h1>
          <PillBadge :type="severityVariant(insight.severity)" :value="insight.severity" />
        </div>
        <p class="page-subtitle">由 {{ insight.agentName || 'AI' }} 生成</p>
      </div>

      <!-- Summary -->
      <div class="card" style="margin-bottom: 16px;">
        <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">Summary</h2>
        <p style="font-size: 13px; color: var(--text-primary); line-height: 1.6;">{{ insight.summary }}</p>
      </div>

      <!-- Risk Score -->
      <div v-if="risk" class="card" style="margin-bottom: 16px;">
        <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">Risk Score</h2>
        <div style="display: flex; align-items: center; gap: 16px;">
          <div style="font-size: 36px; font-weight: 700; color: var(--text-primary);">
            {{ risk.riskScore }}
          </div>
          <div>
            <PillBadge :type="severityVariant(risk.riskLevel)" :value="risk.riskLevel" />
          </div>
        </div>
        <p style="font-size: 13px; color: var(--text-secondary); margin-top: 8px;">{{ risk.reason }}</p>
        <div v-if="risk.ruleName" style="margin-top: 8px;">
          <span class="badge badge-neutral">{{ risk.ruleName }}</span>
        </div>
      </div>

      <!-- Suggestion -->
      <div v-if="insight.suggestion" class="card" style="margin-bottom: 16px;">
        <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">
          <FontAwesomeIcon icon="lightbulb" style="margin-right: 8px;" />Recommendation
        </h2>
        <p style="font-size: 13px; color: var(--text-primary); line-height: 1.6;">{{ insight.suggestion }}</p>
      </div>

      <!-- Evidence Graph -->
      <div v-if="insight.evidenceJson" class="card">
        <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">
          <FontAwesomeIcon icon="link" style="margin-right: 8px;" />Evidence
        </h2>
        <div class="evidence-grid">
          <div v-for="(value, key) in formatEvidence(insight.evidenceJson)" :key="key" class="evidence-item">
            <span class="evidence-key">{{ key }}</span>
            <span class="evidence-value">{{ value }}</span>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-state-icon"><FontAwesomeIcon icon="exclamation-triangle" /></div>
      <div class="empty-state-text">Insight 未找到</div>
    </div>
  </div>
</template>

<style scoped>
.evidence-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

@media (max-width: 768px) {
  .evidence-grid {
    grid-template-columns: 1fr;
  }
}

.evidence-item {
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.evidence-key {
  font-size: 11px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.evidence-value {
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-all;
}
</style>