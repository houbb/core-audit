<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAuditEvent } from '@/api/audit'
import { getRisk } from '@/api/intelligence'
import PillBadge from '@/components/PillBadge.vue'
import JsonViewer from '@/components/JsonViewer.vue'
import type { AuditEvent, AuditRisk } from '@/types/audit'

const route = useRoute()
const router = useRouter()
const event = ref<AuditEvent | null>(null)
const risk = ref<AuditRisk | null>(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const id = route.params.id as string
    event.value = await getAuditEvent(id)
    // 尝试加载风险分析
    try {
      risk.value = await getRisk(id)
    } catch {
      risk.value = null
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/events')
}

function formatTime(iso: string): string {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}

function riskVariant(level: string): 'success' | 'fail' | 'neutral' {
  switch (level) {
    case 'CRITICAL': return 'fail'
    case 'HIGH': return 'fail'
    case 'MEDIUM': return 'neutral'
    default: return 'success'
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="breadcrumb">
      <a href="#" @click.prevent="goBack">审计记录</a>
      <span class="sep">/</span>
      <span>详情</span>
    </div>

    <div v-if="loading" class="empty-state">
      <div class="empty-state-text">加载中…</div>
    </div>

    <template v-else-if="event">
      <div class="page-header">
        <h1 class="page-title">审计详情</h1>
        <p class="page-subtitle">{{ event.description || event.id }}</p>
      </div>

      <div class="card">
        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">ID</span>
            <span class="detail-value text-mono">{{ event.id }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">时间</span>
            <span class="detail-value">{{ formatTime(event.createdAt) }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">模块</span>
            <span class="detail-value"><PillBadge type="module" :value="event.module" /></span>
          </div>
          <div class="detail-field">
            <span class="detail-label">操作</span>
            <span class="detail-value"><PillBadge type="action" :value="event.action" /></span>
          </div>
          <div class="detail-field">
            <span class="detail-label">对象类型</span>
            <span class="detail-value">{{ event.targetType || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">对象 ID</span>
            <span class="detail-value text-mono">{{ event.targetId || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">操作人 ID</span>
            <span class="detail-value text-mono">{{ event.operatorId || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">操作人</span>
            <span class="detail-value">{{ event.operatorName || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">结果</span>
            <span class="detail-value">
              <PillBadge
                :type="event.result === 'SUCCESS' ? 'success' : 'fail'"
                :value="event.result"
              />
            </span>
          </div>
          <div class="detail-field">
            <span class="detail-label">客户端 IP</span>
            <span class="detail-value text-mono">{{ event.clientIp || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">请求 URI</span>
            <span class="detail-value text-mono">{{ event.requestUri || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">请求方法</span>
            <span class="detail-value">{{ event.requestMethod || '-' }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">Trace ID</span>
            <span class="detail-value text-mono">{{ event.traceId || '-' }}</span>
          </div>
          <div class="detail-field" style="grid-column: 1 / -1;">
            <span class="detail-label">描述</span>
            <span class="detail-value">{{ event.description || '-' }}</span>
          </div>
        </div>

        <div style="margin-top: 20px;">
          <JsonViewer :data="event.metadata" />
        </div>
      </div>

      <!-- P8 AI Analysis 面板 -->
      <div v-if="risk" class="card" style="margin-top: 16px;">
        <h2 style="font-size: 14px; font-weight: 600; margin-bottom: 16px;">
          <FontAwesomeIcon icon="chart-bar" style="margin-right: 8px;" />AI Analysis
        </h2>
        <div class="detail-grid">
          <div class="detail-field">
            <span class="detail-label">Risk Score</span>
            <span class="detail-value" style="font-size: 18px; font-weight: 700;">
              {{ risk.riskScore }}
            </span>
          </div>
          <div class="detail-field">
            <span class="detail-label">Risk Level</span>
            <span class="detail-value">
              <PillBadge :type="riskVariant(risk.riskLevel)" :value="risk.riskLevel" />
            </span>
          </div>
          <div class="detail-field" style="grid-column: 1 / -1;">
            <span class="detail-label">Reason</span>
            <span class="detail-value">{{ risk.reason || '-' }}</span>
          </div>
          <div v-if="risk.ruleName" class="detail-field">
            <span class="detail-label">Rule</span>
            <span class="detail-value">
              <span class="badge badge-neutral">{{ risk.ruleName }}</span>
            </span>
          </div>
          <div v-if="risk.aiAnalysis" class="detail-field" style="grid-column: 1 / -1;">
            <span class="detail-label">AI Summary</span>
            <span class="detail-value">{{ risk.aiAnalysis }}</span>
          </div>
          <div class="detail-field">
            <span class="detail-label">Analyzed At</span>
            <span class="detail-value">{{ formatTime(risk.analyzedAt) }}</span>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-state-icon"><FontAwesomeIcon icon="exclamation-triangle" /></div>
      <div class="empty-state-text">未找到该审计事件</div>
    </div>
  </div>
</template>