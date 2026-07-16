import axios from 'axios'
import type { AuditEvent, AuditEventPage, DashboardStats, AuditQuery } from '@/types/audit'

const api = axios.create({
  baseURL: '/api/v1/audit',
  timeout: 10000,
})

// 写入审计
export function createAuditEvent(data: Partial<AuditEvent>): Promise<AuditEvent> {
  return api.post('/events', data).then((res) => res.data)
}

// 分页查询
export function queryAuditEvents(params: AuditQuery): Promise<AuditEventPage> {
  return api.get('/events', { params }).then((res) => res.data)
}

// 查看详情
export function getAuditEvent(id: string): Promise<AuditEvent> {
  return api.get(`/events/${id}`).then((res) => res.data)
}

// 导出 CSV
export function exportAuditEvents(params: AuditQuery): void {
  const qs = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== '') qs.append(k, String(v))
  })
  window.open(`/api/v1/audit/events/export?${qs.toString()}`, '_blank')
}

// Dashboard 统计
export function getDashboardStats(): Promise<DashboardStats> {
  return api.get('/dashboard').then((res) => res.data)
}