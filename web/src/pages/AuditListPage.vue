<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { queryAuditEvents } from '@/api/audit'
import FilterBar from '@/components/FilterBar.vue'
import AuditTable from '@/components/AuditTable.vue'
import Pagination from '@/components/Pagination.vue'
import type { AuditEvent, AuditQuery } from '@/types/audit'

const events = ref<AuditEvent[]>([])
const total = ref(0)
const page = ref(1)
const hasNext = ref(false)
const loading = ref(true)
const currentQuery = ref<AuditQuery>({})

async function search(query: AuditQuery) {
  currentQuery.value = query
  loading.value = true
  try {
    const result = await queryAuditEvents(query)
    events.value = result.items
    total.value = result.total
    page.value = result.page
    hasNext.value = result.hasNext
  } finally {
    loading.value = false
  }
}

function reset() {
  currentQuery.value = {}
  search({ page: 1, size: 20 })
}

function changePage(p: number) {
  const q = { ...currentQuery.value, page: p }
  search(q)
}

onMounted(() => {
  search({ page: 1, size: 20 })
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">审计记录</h1>
      <p class="page-subtitle">所有模块的统一审计事件记录</p>
    </div>

    <FilterBar @search="search" @reset="reset" />

    <AuditTable :events="events" />

    <Pagination
      :page="page"
      :total="total"
      :hasNext="hasNext"
      @change="changePage"
    />
  </div>
</template>
