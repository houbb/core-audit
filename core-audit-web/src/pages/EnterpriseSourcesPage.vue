<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSources, registerSource, deleteSource, sourceHeartbeat } from '@/api/enterprise'

interface Source {
  id: string
  name: string
  type: string
  version: string
  status: string
  registeredAt: string
  lastSeenAt: string
  description: string
  endpoint: string
}

const sources = ref<Source[]>([])
const loading = ref(true)
const showForm = ref(false)
const form = ref({ name: '', type: 'INTERNAL', version: '1.0', description: '', endpoint: '' })

onMounted(async () => {
  await loadSources()
})

async function loadSources() {
  loading.value = true
  try {
    sources.value = await getSources()
  } catch (e) {
    console.error('Failed to load sources', e)
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  try {
    await registerSource(form.value)
    showForm.value = false
    form.value = { name: '', type: 'INTERNAL', version: '1.0', description: '', endpoint: '' }
    await loadSources()
  } catch (e) {
    console.error('Failed to register source', e)
  }
}

async function handleDelete(id: string) {
  if (!confirm('确认删除此来源系统？')) return
  try {
    await deleteSource(id)
    await loadSources()
  } catch (e) {
    console.error('Failed to delete source', e)
  }
}

async function handleHeartbeat(id: string) {
  try {
    await sourceHeartbeat(id)
    await loadSources()
  } catch (e) {
    console.error('Failed to send heartbeat', e)
  }
}

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    INTERNAL: '内部系统', EXTERNAL: '外部系统', PLUGIN: '插件', AGENT: 'AI Agent', THIRD_PARTY: '第三方'
  }
  return map[type] || type
}
</script>

<template>
  <div class="sources-page">
    <div class="page-header">
      <div>
        <h1><FontAwesomeIcon icon="server" /> 来源系统管理</h1>
        <p class="subtitle">管理所有接入 core-audit 的系统身份</p>
      </div>
      <button class="btn btn-primary" @click="showForm = !showForm">
        <FontAwesomeIcon icon="plus" /> 注册来源
      </button>
    </div>

    <!-- Register Form -->
    <div v-if="showForm" class="form-panel">
      <h3>注册新来源系统</h3>
      <div class="form-grid">
        <label>
          系统名称*
          <input v-model="form.name" placeholder="如 core-user" />
        </label>
        <label>
          类型
          <select v-model="form.type">
            <option value="INTERNAL">内部系统</option>
            <option value="EXTERNAL">外部系统</option>
            <option value="PLUGIN">插件</option>
            <option value="AGENT">AI Agent</option>
            <option value="THIRD_PARTY">第三方</option>
          </select>
        </label>
        <label>
          SDK 版本
          <input v-model="form.version" placeholder="1.0" />
        </label>
        <label>
          回调地址
          <input v-model="form.endpoint" placeholder="https://..." />
        </label>
      </div>
      <label style="display:block;margin-top:12px">
        描述
        <input v-model="form.description" placeholder="系统描述（可选）" style="width:100%" />
      </label>
      <div class="form-actions">
        <button class="btn btn-primary" @click="handleRegister">保存</button>
        <button class="btn" @click="showForm = false">取消</button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <!-- Sources Table -->
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>类型</th>
          <th>版本</th>
          <th>状态</th>
          <th>注册时间</th>
          <th>最后活跃</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in sources" :key="s.id">
          <td class="name-cell">{{ s.name }}</td>
          <td><span class="pill">{{ typeLabel(s.type) }}</span></td>
          <td>{{ s.version }}</td>
          <td><span class="pill" :class="s.status === 'ACTIVE' ? 'success' : 'warning'">{{ s.status }}</span></td>
          <td>{{ s.registeredAt }}</td>
          <td>{{ s.lastSeenAt || '-' }}</td>
          <td class="actions">
            <button class="btn btn-sm" @click="handleHeartbeat(s.id)"><FontAwesomeIcon icon="heart-pulse" /></button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(s.id)"><FontAwesomeIcon icon="trash" /></button>
          </td>
        </tr>
        <tr v-if="sources.length === 0">
          <td colspan="7" class="empty">暂无来源系统，请注册</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.sources-page {
  padding: 32px;
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.subtitle { font-size: 13px; color: var(--text-secondary); margin: 4px 0 0; }

.form-panel {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
}

.form-panel h3 { font-size: 14px; margin: 0 0 12px; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

label { font-size: 12px; color: var(--text-secondary); }
label input, label select {
  display: block;
  width: 100%;
  margin-top: 4px;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 13px;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.form-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.loading { text-align: center; padding: 60px; color: var(--text-secondary); }

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 2px solid var(--border);
}

.data-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
}

.name-cell { font-weight: 600; }

.pill {
  display: inline-block;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 11px;
}

.pill.success { background: rgba(22, 163, 74, 0.1); color: #16a34a; }
.pill.warning { background: rgba(234, 179, 8, 0.1); color: #ca8a04; }

.actions { display: flex; gap: 4px; }

.empty { text-align: center; color: var(--text-secondary); padding: 40px; }
</style>