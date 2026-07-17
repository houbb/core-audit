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