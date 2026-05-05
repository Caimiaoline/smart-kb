import axios from 'axios'

const http = axios.create({
  baseURL: '',
  timeout: 30000
})

export async function listApplications() {
  const { data } = await http.get('/api/applications')
  return data
}

export async function createApplication(payload) {
  const { data } = await http.post('/api/applications', payload)
  return data
}

export async function listKnowledgeBases() {
  const { data } = await http.get('/api/knowledge')
  return data
}

export async function createKnowledgeBase(payload) {
  const { data } = await http.post('/api/knowledge/create', payload)
  return data
}

export async function uploadKnowledgeDocument(kbId, file) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await http.post(`/api/knowledge/${kbId}/upload`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return data
}

export async function listKnowledgeDocuments(kbId) {
  const { data } = await http.get(`/api/knowledge/${kbId}/documents`)
  return data
}

export async function hitTestKnowledge(kbId, payload) {
  const { data } = await http.post(`/api/knowledge/${kbId}/hit-test`, payload)
  return data
}

export async function routeAgent(payload) {
  const { data } = await http.post('/api/agent/route', payload)
  return data
}

export async function listTools() {
  const { data } = await http.get('/api/tools')
  return data
}

export async function executeTool(payload) {
  const { data } = await http.post('/api/tools/execute', payload)
  return data
}

export async function listMcpTools() {
  const { data } = await http.get('/api/mcp/tools')
  return data
}

export async function getAgentMetrics() {
  const { data } = await http.get('/api/metrics/agent')
  return data
}
