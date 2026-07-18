<script setup lang="ts">
import { ref } from 'vue'
import { globalSearch } from '@/api/enterprise'

const query = ref('')
const results = ref<any>(null)
const searching = ref(false)

async function doSearch() {
  if (!query.value.trim()) return
  searching.value = true
  try {
    results.value = await globalSearch(query.value)
  } catch (e) {
    console.error('Search failed', e)
  } finally {
    searching.value = false
  }
}
</script>

<template>
  <div class="search-page">
    <div class="page-header">
      <h1><FontAwesomeIcon icon="magnifying-glass" /> Global Search</h1>
      <p class="subtitle">企业全局搜索 — 跨模块检索所有审计事件</p>
    </div>

    <!-- Search Bar -->
    <div class="search-bar">
      <input
        v-model="query"
        placeholder="搜索审计事件...（输入关键字搜索 description / targetId / operatorName）"
        @keyup.enter="doSearch"
      />
      <button class="btn btn-primary" @click="doSearch" :disabled="searching">
        <FontAwesomeIcon icon="magnifying-glass" /> {{ searching ? '搜索中...' : '搜索' }}
      </button>
    </div>

    <!-- Results -->
    <div v-if="results" class="results">
      <div class="results-header">
        <span>搜索 "<strong>{{ results.keyword }}</strong>" — 共 <strong>{{ results.totalHits }}</strong> 条结果</span>
        <span class="sources">搜索范围: {{ results.sourcesSearched.join(', ') }}</span>
      </div>

      <!-- Module Distribution -->
      <div v-if="results.moduleDistribution" class="module-dist">
        <span v-for="(count, mod) in results.moduleDistribution" :key="mod" class="pill">
          {{ mod }}: {{ count }}
        </span>
      </div>

      <!-- Items -->
      <div v-if="results.items?.length" class="result-list">
        <div v-for="item in results.items" :key="item.id" class="result-item">
          <div class="result-top">
            <span class="pill" :class="item.result === 'SUCCESS' ? 'success' : 'fail'">{{ item.result }}</span>
            <span class="result-module">{{ item.module }}</span>
            <span class="result-action">{{ item.action }}</span>
          </div>
          <p class="result-desc">{{ item.description }}</p>
          <div class="result-meta">
            <span>{{ item.operatorName }}</span>
            <span>{{ item.targetType }}: {{ item.targetId }}</span>
            <span>{{ item.createdAt }}</span>
          </div>
        </div>
      </div>
      <div v-else class="no-results">无结果</div>
    </div>

    <div v-else class="empty-state">
      <FontAwesomeIcon icon="search" class="empty-icon" />
      <p>输入关键字开始搜索</p>
    </div>
  </div>
</template>

<style scoped>
.search-page { padding: 32px; max-width: 1000px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 20px; font-weight: 700; margin: 0; }
.subtitle { font-size: 13px; color: var(--text-secondary); margin: 4px 0 0; }

.search-bar { display: flex; gap: 10px; margin-bottom: 28px; }
.search-bar input { flex: 1; padding: 12px 16px; border: 1px solid var(--border); border-radius: var(--radius-md); font-size: 14px; background: var(--bg-secondary); color: var(--text-primary); }
.search-bar input:focus { outline: none; border-color: var(--accent); }

.results-header { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 12px; color: var(--text-secondary); }
.sources { font-size: 11px; }

.module-dist { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 20px; }

.result-list { display: flex; flex-direction: column; gap: 12px; }

.result-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 16px;
}

.result-top { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.result-module { font-weight: 600; color: var(--accent); font-size: 13px; }
.result-action { font-size: 12px; color: var(--text-secondary); }

.pill { border-radius: 10px; padding: 2px 8px; font-size: 11px; }
.pill.success { background: rgba(22, 163, 74, 0.1); color: #16a34a; }
.pill.fail { background: rgba(220, 38, 38, 0.1); color: #dc2626; }

.result-desc { font-size: 14px; margin: 0 0 8px; color: var(--text-primary); }

.result-meta { display: flex; gap: 16px; font-size: 11px; color: var(--text-secondary); }

.no-results, .empty-state { text-align: center; padding: 60px; color: var(--text-secondary); }
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; opacity: 0.3; }
</style>