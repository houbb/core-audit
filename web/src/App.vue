<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const currentRoute = router.currentRoute

function isActive(path: string): boolean {
  return currentRoute.value.path === path
}
</script>

<template>
  <div class="app">
    <aside class="sidebar">
      <div class="brand" @click="router.push('/')">
        <span class="brand-icon">📋</span>
        <span class="brand-text">core-audit</span>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-item" :class="{ active: isActive('/') }">
          <span class="nav-icon">◫</span>
          <span>概览</span>
        </router-link>
        <router-link to="/events" class="nav-item" :class="{ active: isActive('/events') || currentRoute.path.startsWith('/events/') }">
          <span class="nav-icon">☰</span>
          <span>审计记录</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <span class="version">v1.0.0</span>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

/* ---- Sidebar ---- */
.sidebar {
  width: 220px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  padding: 0;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  cursor: pointer;
  user-select: none;
}

.brand-icon {
  font-size: 20px;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.nav {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-primary);
  text-decoration: none;
  transition: background 0.15s;
}

.nav-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.nav-item.active {
  background: var(--accent);
  color: var(--color-on-accent);
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border);
}

.version {
  font-size: 11px;
  color: var(--text-secondary);
}

/* ---- Main ---- */
.main {
  flex: 1;
  overflow-x: hidden;
}
</style>
