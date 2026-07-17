export interface AuditEvent {
  id: string
  module: string
  action: string
  targetType: string
  targetId: string
  operatorId: string
  operatorName: string
  result: string
  description: string
  clientIp: string
  requestUri: string
  requestMethod: string
  traceId: string
  createdAt: string
  metadata: Record<string, unknown> | null
}

export interface AuditEventPage {
  items: AuditEvent[]
  page: number
  size: number
  total: number
  hasNext: boolean
}

export interface DashboardStats {
  todayTotal: number
  todaySuccess: number
  todayFail: number
  activeModules: number
  recentEvents: AuditEvent[]
  // P5 timeline stats
  todayTimelineCount: number
  avgTimelineLength: number
  maxTimelineLength: number
  avgTimelineDuration: number
  // P8 intelligence stats
  todayInsightCount: number
  todayCriticalCount: number
  todayHighCount: number
  avgRiskScore: number
}

export interface AuditQuery {
  page?: number
  size?: number
  module?: string
  action?: string
  operator?: string
  result?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

// ======== P5 Timeline types ========

export interface Timeline {
  id: string
  type: string
  title: string
  startTime: string
  endTime: string
  duration: number
  summary?: string
  events: AuditEvent[]
  createdAt: string
}

// ======== P8 Intelligence types ========

export interface AuditRisk {
  auditId: string
  riskScore: number
  riskLevel: string
  reason: string
  ruleName: string
  aiAnalysis: string
  analyzedAt: string
}

export interface AuditInsight {
  id: string
  auditId: string | null
  title: string
  severity: string
  summary: string
  suggestion: string
  evidenceJson: string
  agentName: string
}

export interface IntelligenceDashboard {
  todayInsightCount: number
  todayCriticalCount: number
  todayHighCount: number
  todayMediumCount: number
  avgRiskScore: number
}