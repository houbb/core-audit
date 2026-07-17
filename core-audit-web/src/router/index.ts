import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/pages/DashboardPage.vue'),
    },
    {
      path: '/events',
      name: 'audit-list',
      component: () => import('@/pages/AuditListPage.vue'),
    },
    {
      path: '/events/:id',
      name: 'audit-detail',
      component: () => import('@/pages/AuditDetailPage.vue'),
    },
    // ======== P5 Timeline routes ========
    {
      path: '/timeline/:id',
      name: 'timeline-detail',
      component: () => import('@/pages/TimelinePage.vue'),
    },
    {
      path: '/object/:id/timeline',
      name: 'object-timeline',
      component: () => import('@/pages/TimelineListPage.vue'),
    },
    {
      path: '/operator/:id/timeline',
      name: 'operator-timeline',
      component: () => import('@/pages/TimelineListPage.vue'),
    },
    {
      path: '/trace/:traceId',
      name: 'trace-timeline',
      component: () => import('@/pages/TimelineListPage.vue'),
    },
    // ======== P8 Intelligence routes ========
    {
      path: '/intelligence',
      name: 'intelligence-dashboard',
      component: () => import('@/pages/IntelligencePage.vue'),
    },
    {
      path: '/intelligence/insights/:id',
      name: 'intelligence-detail',
      component: () => import('@/pages/InsightDetailPage.vue'),
    },
  ],
})

export default router