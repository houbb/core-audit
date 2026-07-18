<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProviders, installProvider, uninstallProvider, updateProviderStatus, getProvidersByType } from '@/api/enterprise'

interface Provider {
  id: string
  plugin: string
  providerType: string
  version: string
  description: string
  author: string
  status: string
  installedAt: string
}

const providers = ref<Provider[]>([])
const loading = ref(true)
const showForm = ref(false)
const activeType = ref('ALL')
const form = ref({ plugin: '', providerClass: '', providerType: 'PROVIDER', version: '1.0', description: '', author: '' })

const typeOptions = ['ALL', 'PROVIDER', 'TIMELINE_STRATEGY', 'REPLAY_STRATEGY', 'COMPLIANCE_PROVIDER', 'AUDIT_AGENT']

onMounted(() => loadProviders())

async function loadProviders() {
  loading.value = true
  try {
    if (activeType.value === 'ALL') {
      providers.value = await getProviders()
    } else {
      providers.value = await getProvidersByType(activeType.value)
    }
  } catch (e) { console.error('Failed to load providers', e) }
  finally { loading.value = false }
}

async function handleInstall() {
  try {
    await installProvider(form.value)
    showForm.value = false
    form.value = { plugin: '', providerClass: '', providerType: 'PROVIDER', version: '1.0', description: '', author: '' }
    await loadProviders()
  } catch (e) { console.error('Failed to install provider', e) }
}

async function handleToggle(provider: Provider) {
  const newStatus = provider.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await updateProviderStatus(provider.id, newStatus)
    await loadProviders()
  } catch (e) { console.error('Failed to update status', e) }
}

async function handleUninstall(id: string) {
  if (!confirm('确认卸载此插件？')) return
  try {
    await uninstallProvider(id)
    await loadProviders()
  } catch (e) { console.error('Failed to uninstall', e) }
}

function typeLabel(t: string): string {
  const map: Record<string, string> = {
    PROVIDER: 'Provider', TIMELINE_STRATEGY: 'Timeline 策略',
    REPLAY_STRATEGY: 'Replay 策略', COMPLIANCE_PROVIDER: '合规策略', AUDIT_AGENT: 'AI Agent'
  }
  return map[t] || t
}
</script>

<template>
  <div class="providers-page">
    <div class="page-header">
      <div>
        <h1><FontAwesomeIcon icon="puzzle-piece" /> Plugin Marketplace</h1>
        <p class="subtitle">管理 SPI 插件安装与 Marketplace</p>
      </div>
      <button class="btn btn-primary" @click="showForm = !showForm">
        <FontAwesomeIcon icon="plus" /> 安装插件
      </button>
    </div>

    <!-- Install Form -->
    <div v-if="showForm" class="form-panel">
      <h3>安装新插件</h3>
      <div class="form-grid">
        <label>插件名称* <input v-model="form.plugin" placeholder="如 sap-audit" /></label>
        <label>实现类全限定名* <input v-model="form.providerClass" placeholder="com.example.SapAuditProvider" /></label>
        <label>
          插件类型
          <select v-model="form.providerType">
            <option value="PROVIDER">Provider</option>
            <option value="TIMELINE_STRATEGY">Timeline Strategy</option>
            <option value="REPLAY_STRATEGY">Replay Strategy</option>
            <option value="COMPLIANCE_PROVIDER">Compliance Provider</option>
            <option value="AUDIT_AGENT">Audit Agent</option>
          </select>
        </label>
        <label>版本 <input v-model="form.version" placeholder="1.0" /></label>
      </div>
      <label style="display:block;margin-top:12px">描述 <input v-model="form.description" placeholder="插件描述" style="width:100%" /></label>
      <label style="display:block;margin-top:8px">作者 <input v-model="form.author" placeholder="作者" style="width:100%" /></label>
      <div class="form-actions">
        <button class="btn btn-primary" @click="handleInstall">安装</button>
        <button class="btn" @click="showForm = false">取消</button>
      </div>
    </div>

    <!-- Type Filter -->
    <div class="filter-bar">
      <button v-for="t in typeOptions" :key="t" class="btn btn-sm" :class="{ active: activeType === t }" @click="activeType = t; loadProviders()">
        {{ t === 'ALL' ? '全部' : typeLabel(t) }}
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <!-- Provider Cards -->
    <div v-else class="provider-grid">
      <div v-for="p in providers" :key="p.id" class="provider-card">
        <div class="card-header">
          <span class="provider-name">{{ p.plugin }}</span>
          <span class="pill" :class="p.status === 'ACTIVE' ? 'success' : 'warning'">{{ p.status }}</span>
        </div>
        <div class="card-body">
          <div class="meta"><span>类型</span><span>{{ typeLabel(p.providerType) }}</span></div>
          <div class="meta"><span>版本</span><span>{{ p.version }}</span></div>
          <div class="meta"><span>作者</span><span>{{ p.author || '-' }}</span></div>
          <p class="desc">{{ p.description || '无描述' }}</p>
        </div>
        <div class="card-footer">
          <button class="btn btn-sm" @click="handleToggle(p)">
            {{ p.status === 'ACTIVE' ? '禁用' : '启用' }}
          </button>
          <button class="btn btn-sm btn-danger" @click="handleUninstall(p.id)">卸载</button>
        </div>
      </div>
      <div v-if="providers.length === 0" class="empty">暂无已安装插件</div>
    </div>
  </div>
</template>

<style scoped>
.providers-page {
  padding: 32px;
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 { font-size: 20px; font-weight: 700; margin: 0; }
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
  display: block; width: 100%; margin-top: 4px; padding: 8px 10px;
  border: 1px solid var(--border); border-radius: 6px; font-size: 13px;
  background: var(--bg-primary); color: var(--text-primary);
}

.form-actions { margin-top: 16px; display: flex; gap: 8px; }

.filter-bar { display: flex; gap: 6px; margin-bottom: 20px; flex-wrap: wrap; }
.filter-bar .btn.active { background: var(--accent); color: var(--color-on-accent); }

.loading { text-align: center; padding: 60px; color: var(--text-secondary); }

.provider-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.provider-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.provider-name {
  font-size: 14px;
  font-weight: 600;
}

.pill {
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 11px;
}

.pill.success { background: rgba(22, 163, 74, 0.1); color: #16a34a; }
.pill.warning { background: rgba(234, 179, 8, 0.1); color: #ca8a04; }

.card-body { margin-bottom: 12px; }
.meta { display: flex; justify-content: space-between; font-size: 12px; padding: 3px 0; color: var(--text-secondary); }
.desc { font-size: 12px; color: var(--text-secondary); margin-top: 8px; }

.card-footer { display: flex; gap: 6px; border-top: 1px solid var(--border); padding-top: 12px; }

.empty { text-align: center; color: var(--text-secondary); padding: 40px; grid-column: 1 / -1; }
</style>