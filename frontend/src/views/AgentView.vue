<template>
  <section class="card" style="padding: 18px;">
    <div class="panel-grid">
      <div class="panel-soft">
        <div class="section-title" style="margin-bottom: 10px;">Agent 任务入口</div>
        <div style="font-size: 12px; color: var(--text-tertiary); margin-bottom: 12px;">输入自然语言，系统会路由到 RAG、应用草案或工具执行链路。</div>
        <textarea v-model="form.input" class="form-control" rows="5" placeholder="例如：如何上传知识库文档？" />
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:10px;">
          <input v-model.number="form.kbId" class="form-control" type="number" placeholder="kbId 可选" />
          <input v-model.number="form.applicationId" class="form-control" type="number" placeholder="appId 可选" />
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:12px;" @click="run" :disabled="loading"><Play :size="15" />执行 Agent 路由</button>
      </div>

      <div class="card" style="padding: 16px;">
        <div class="toolbar" style="margin-bottom: 12px;">
          <div class="section-title">执行结果</div>
          <span class="badge badge-gray">{{ result?.route || '等待执行' }}</span>
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:12px;">
          <div class="panel-soft"><div class="stat-label">选中工具</div><div class="stat-value" style="margin-top:6px;">{{ result?.selectedTool || '-' }}</div></div>
          <div class="panel-soft"><div class="stat-label">Trace</div><div class="stat-value" style="margin-top:6px;">{{ traceText }}</div></div>
        </div>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:12px;">
          <div class="panel-soft"><div class="stat-label">路由原因</div><div style="margin-top:6px;font-size:12px;line-height:18px;">{{ reasonText }}</div></div>
          <div class="panel-soft"><div class="stat-label">执行状态</div><div style="margin-top:6px;font-size:12px;">{{ statusText }}</div></div>
          <div class="panel-soft"><div class="stat-label">工具消息</div><div style="margin-top:6px;font-size:12px;line-height:18px;">{{ messageText }}</div></div>
        </div>
        <div class="panel-soft" style="margin-bottom: 12px;">
          <div class="stat-label" style="margin-bottom: 8px;">执行计划</div>
          <div v-if="!plan.length" class="empty-state" style="padding:20px;">暂无执行计划</div>
          <div v-else class="trace-list">
            <div v-for="(step, idx) in plan" :key="idx" class="trace-item"><div style="display:flex;align-items:center;gap:10px;"><span class="badge">{{ idx + 1 }}</span><span>{{ step }}</span></div></div>
          </div>
        </div>
        <div class="panel-soft" style="margin-bottom: 12px;">
          <div class="stat-label" style="margin-bottom: 8px;">Trace Events</div>
          <div v-if="!events.length" class="empty-state" style="padding:20px;">暂无 Trace 事件</div>
          <div v-else class="trace-list">
            <div v-for="(event, idx) in events" :key="idx" class="trace-item">
              <div><div style="font-family:ui-monospace,monospace;font-size:12px;">{{ event.stage }}</div><div class="entity-id">{{ event.timestamp }}</div></div>
              <span :class="['badge', event.status === 'completed' ? '' : 'badge-gray']">{{ event.status }}</span>
            </div>
          </div>
        </div>
        <pre class="json-box">{{ resultJson }}</pre>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Play } from 'lucide-vue-next'
import { getAgentMetrics, routeAgent } from '../api'

const form = reactive({ input: '', kbId: null, applicationId: null })
const loading = ref(false)
const result = ref(null)
const metrics = ref(null)

const plan = computed(() => result.value?.plan || [])
const events = computed(() => result.value?.metadata?.trace?.events || [])
const traceText = computed(() => {
  const trace = result.value?.metadata?.trace
  return trace ? `${trace.durationMs} ms / success=${trace.success}` : '-'
})
const reasonText = computed(() => {
  if (!result.value) return '-'
  return `${result.value.reason || '-'}${result.value.metadata?.llmRouterUsed ? '（LLM Router）' : '（Rule Router）'}`
})
const statusText = computed(() => result.value ? `${result.value.execution?.success ? 'success' : 'failed'} / ${result.value.metadata?.status || '-'}` : '-')
const messageText = computed(() => result.value ? `${result.value.execution?.message || '-'} / taskId=${result.value.metadata?.taskId || '-'}` : '-')
const resultJson = computed(() => JSON.stringify(result.value || { metrics: metrics.value }, null, 2))

async function run() {
  if (!form.input?.trim()) return
  loading.value = true
  try {
    result.value = await routeAgent({ ...form })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  metrics.value = await getAgentMetrics().catch(() => null)
})
</script>
