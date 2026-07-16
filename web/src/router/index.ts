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
  ],
})

export default router