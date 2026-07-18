<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSubscriptions, saveSubscription, deleteSubscription } from '@/api/enterprise'

interface Subscription {
  id: string
  subscriber: string
  eventType: string | null
  module: string | null
  target: string
  targetUrl: string
  enabled: number
  lastSentAt: string | null
  lastStatus: string | null
}

const subscriptions = ref<Subscription[]>([])
const loading = ref(true)
const showForm = ref(false)
const form = ref({
  subscriber: '', eventType: '', module: '', target: 'WEBHOOK',
  targetUrl: '', retryCount: 3, timeoutMs: 5000
})

onMounted(() => loadSubscriptions())

async function loadSubscriptions() {
  loading.value = true
  try { subscriptions.value = await getSubscriptions() }
  catch (e) { console.error('Failed to load subscriptions', e) }
  finally { loading.value = false }
}

async function handleSave() {
  try {
    await saveSubscription({ ...form.value, enabled: 1 })
    showForm.value = false
    form.value = { subscriber: '', eventType: '', module: '', target: 'WEBHOOK', targetUrl: '', retryCount: 3, timeoutMs: 5000 }
    await loadSubscriptions()
  } catch (e) { console.error('Failed to save subscription', e) }
}

async function handleDelete(id: string) {
  if (!confirm('确认删除此订阅？')) return
  try { await deleteSubscription(id); await loadSubscriptions() }
  catch (e) { console.error('Failed to delete', e) }
}
</script>

<template>
  <div class="subscriptions-page">
    <div class="page-header">
      <div>
        <h1><FontAwesomeIcon icon="bell" /> Webhook 订阅管理</h1>
        <p class="subtitle">管理审计事件的 Webhook 通知订阅</p>
      </div>
      <button class="btn btn-primary" @click="showForm = !showForm">
        <FontAwesomeIcon icon="plus" /> 新增订阅
      </button>
    </div>

    <div v-if="showForm" class="form-panel">
      <h3>新增订阅</h3>
      <div class="form-grid">
        <label>订阅方名称* <input v-model="form.subscriber" placeholder="如 Slack" /></label>
        <label>目标类型
          <select v-model="form.target">
            <option value="WEBHOOK">Webhook</option>
            <option value="LOCAL">本地</option>
            <option value="STREAM">流</option>
            <option value="QUEUE">队列</option>
          </select>
        </label>
        <label>目标 URL <input v-model="form.targetUrl" placeholder="https://hooks.slack.com/..." /></label>
        <label>事件类型（空=所有） <input v-model="form.eventType" placeholder="USER_CREATED" /></label>
        <label>模块过滤（空=所有） <input v-model="form.module" placeholder="USER" /></label>
        <label>
          最大重试次数
          <input v-model.number="form.retryCount" type="number" min="0" max="10" />
        </label>
        <label>
          超时 (ms)
          <input v-model.number="form.timeoutMs" type="number" min="500" step="500" />
        </label>
      </div>
      <div class="form-actions">
        <button class="btn btn-primary" @click="handleSave">保存</button>
        <button class="btn" @click="showForm = false">取消</button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>订阅方</th>
          <th>事件类型</th>
          <th>模块</th>
          <th>目标</th>
          <th>状态</th>
          <th>最后发送</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in subscriptions" :key="s.id">
          <td class="name-cell">{{ s.subscriber }}</td>
          <td>{{ s.eventType || '全部' }}</td>
          <td>{{ s.module || '全部' }}</td>
          <td><span class="pill">{{ s.target }}</span></td>
          <td><span class="pill" :class="s.enabled ? 'success' : 'warning'">{{ s.enabled ? '启用' : '禁用' }}</span></td>
          <td>{{ s.lastSentAt || '-' }}</td>
          <td class="actions">
            <button class="btn btn-sm btn-danger" @click="handleDelete(s.id)"><FontAwesomeIcon icon="trash" /></button>
          </td>
        </tr>
        <tr v-if="subscriptions.length === 0">
          <td colspan="7" class="empty">暂无订阅</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.subscriptions-page { padding: 32px; max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-header h1 { font-size: 20px; font-weight: 700; margin: 0; }
.subtitle { font-size: 13px; color: var(--text-secondary); margin: 4px 0 0; }

.form-panel { background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px; margin-bottom: 24px; }
.form-panel h3 { font-size: 14px; margin: 0 0 12px; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
label { font-size: 12px; color: var(--text-secondary); }
label input, label select { display: block; width: 100%; margin-top: 4px; padding: 8px 10px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px; background: var(--bg-primary); color: var(--text-primary); }
.form-actions { margin-top: 16px; display: flex; gap: 8px; }

.loading { text-align: center; padding: 60px; color: var(--text-secondary); }

.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table th { text-align: left; padding: 10px 12px; font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border); }
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--border); }
.name-cell { font-weight: 600; }
.pill { display: inline-block; border-radius: 10px; padding: 2px 8px; font-size: 11px; }
.pill.success { background: rgba(22, 163, 74, 0.1); color: #16a34a; }
.pill.warning { background: rgba(234, 179, 8, 0.1); color: #ca8a04; }
.actions { display: flex; gap: 4px; }
.empty { text-align: center; color: var(--text-secondary); padding: 40px; }
</style>