import axios from 'axios'
import type { AuditInsight, AuditRisk, IntelligenceDashboard } from '@/types/audit'

const api = axios.create({
  baseURL: '/api/v1/audit/intelligence',
  timeout: 10000,
})

/** Intelligence Dashboard 统计 */
export function getIntelligenceDashboard(): Promise<IntelligenceDashboard> {
  return api.get('/dashboard').then((res) => res.data)
}

/** 分页查询 Insights */
export function queryInsights(page = 1, size = 20): Promise<AuditInsight[]> {
  return api.get('/insights', { params: { page, size } }).then((res) => res.data)
}

/** 获取单个 Insight 详情 */
export function getInsight(id: string): Promise<AuditInsight> {
  return api.get(`/insights/${id}`).then((res) => res.data)
}

/** 获取指定 Audit 的风险评分 */
export function getRisk(auditId: string): Promise<AuditRisk> {
  return api.get(`/risk/${auditId}`).then((res) => res.data)
}

/** 获取全局建议列表 */
export function getRecommendations(limit = 10): Promise<AuditInsight[]> {
  return api.get('/recommendations', { params: { limit } }).then((res) => res.data)
}