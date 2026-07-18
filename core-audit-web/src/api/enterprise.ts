import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1/audit/enterprise',
  timeout: 10000,
})

// ======== Overview ========
export function getEnterpriseOverview(params?: { tenant?: string }) {
  return api.get('/overview', { params }).then((res) => res.data)
}

export function getEnterpriseHealth() {
  return api.get('/health').then((res) => res.data)
}

// ======== Sources ========
export function getSources(tenant = 'default') {
  return api.get('/sources', { params: { tenant } }).then((res) => res.data)
}

export function getActiveSources(tenant = 'default') {
  return api.get('/sources/active', { params: { tenant } }).then((res) => res.data)
}

export function registerSource(data: Record<string, string>) {
  return api.post('/sources', data).then((res) => res.data)
}

export function sourceHeartbeat(id: string) {
  return api.post(`/sources/${id}/heartbeat`)
}

export function deleteSource(id: string) {
  return api.delete(`/sources/${id}`)
}

// ======== Providers (Marketplace) ========
export function getProviders(tenant = 'default') {
  return api.get('/providers', { params: { tenant } }).then((res) => res.data)
}

export function getActiveProviders(tenant = 'default') {
  return api.get('/providers/active', { params: { tenant } }).then((res) => res.data)
}

export function getProvidersByType(providerType: string, tenant = 'default') {
  return api.get(`/providers/type/${providerType}`, { params: { tenant } }).then((res) => res.data)
}

export function installProvider(data: Record<string, string>) {
  return api.post('/providers', data).then((res) => res.data)
}

export function updateProviderStatus(id: string, status: string) {
  return api.put(`/providers/${id}/status`, { status })
}

export function uninstallProvider(id: string) {
  return api.delete(`/providers/${id}`)
}

// ======== Subscriptions (Webhook) ========
export function getSubscriptions() {
  return api.get('/subscriptions').then((res) => res.data)
}

export function getEnabledSubscriptions() {
  return api.get('/subscriptions/enabled').then((res) => res.data)
}

export function saveSubscription(data: Record<string, unknown>) {
  return api.post('/subscriptions', data).then((res) => res.data)
}

export function deleteSubscription(id: string) {
  return api.delete(`/subscriptions/${id}`)
}

export function getSubscriptionDeliveries(id: string, limit = 20) {
  return api.get(`/subscriptions/${id}/deliveries`, { params: { limit } }).then((res) => res.data)
}

// ======== Global Search ========
export function globalSearch(q: string, page = 1, size = 20, tenant = 'default') {
  return axios.get('/api/v1/audit/search', { params: { q, page, size, tenant } }).then((res) => res.data)
}

export function searchSuggest(q: string, limit = 5) {
  return axios.get('/api/v1/audit/search/suggest', { params: { q, limit } }).then((res) => res.data)
}