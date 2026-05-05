<template>
  <section class="card" style="padding: 18px;">
    <div class="panel-grid">
      <div class="panel-soft">
        <div class="section-title" style="margin-bottom: 10px;">工具调试台</div>
        <div style="font-size:12px;color:var(--text-tertiary);margin-bottom:12px;">直接选择工具并传入 JSON 参数，用于验证 Tool Schema 和执行协议。</div>
        <select v-model="selectedToolName" class="form-control" @change="fillTemplate">
          <option value="">请选择工具</option>
          <option v-for="tool in tools" :key="tool.name" :value="tool.name">{{ tool.name }}</option>
        </select>
        <textarea v-model="argumentsText" class="form-control" rows="11" style="margin-top:10px;font-family:ui-monospace,monospace;" />
        <div style="display:flex;gap:10px;margin-top:12px;">
          <button class="btn" style="flex:1" @click="fillTemplate"><FileJson :size="14" />填充模板</button>
          <button class="btn btn-primary" style="flex:1" @click="execute" :disabled="loading"><Terminal :size="14" />执行工具</button>
        </div>
      </div>

      <div class="card" style="padding: 16px;">
        <div class="toolbar"><div class="section-title">工具执行结果</div><button class="btn" @click="load"><RefreshCcw :size="14" />刷新工具</button></div>
        <div class="panel-soft" style="margin-bottom: 12px;">
          <template v-if="selectedTool">
            <div style="font-family:ui-monospace,monospace;font-weight:650;font-size:13px;">{{ selectedTool.name }}</div>
            <div style="font-size:12px;color:var(--text-tertiary);margin-top:6px;">{{ selectedTool.description }}</div>
            <div style="display:flex;flex-direction:column;gap:8px;margin-top:12px;">
              <div v-for="param in selectedTool.parameters || []" :key="param.name" class="trace-item">
                <div><strong style="font-family:ui-monospace,monospace;font-size:12px;">{{ param.name }}</strong><span style="color:var(--text-tertiary);font-size:12px;"> : {{ param.type }}</span><div class="entity-id">{{ param.description }}</div></div>
                <span :class="['badge', param.required ? '' : 'badge-gray']">{{ param.required ? '必填' : '选填' }}</span>
              </div>
            </div>
          </template>
          <span v-else style="font-size:12px;color:var(--text-tertiary);">选择工具后显示参数 schema。</span>
        </div>
        <pre class="json-box">{{ resultText }}</pre>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { FileJson, RefreshCcw, Terminal } from 'lucide-vue-next'
import { executeTool, listTools } from '../api'

const tools = ref([])
const selectedToolName = ref('')
const argumentsText = ref('{}')
const resultText = ref('等待执行工具...')
const loading = ref(false)
const selectedTool = computed(() => tools.value.find(tool => tool.name === selectedToolName.value))

function buildTemplate(tool) {
  const args = {}
  for (const param of tool?.parameters || []) {
    if (param.defaultValue !== null && param.defaultValue !== undefined) args[param.name] = param.defaultValue
    else if (param.type === 'string') args[param.name] = ''
    else if (param.type === 'long' || param.type === 'int') args[param.name] = 1
    else if (param.type === 'double') args[param.name] = 0.35
    else if (param.type === 'boolean') args[param.name] = false
    else args[param.name] = null
  }
  return JSON.stringify(args, null, 2)
}
function fillTemplate() {
  if (!selectedTool.value) return
  argumentsText.value = buildTemplate(selectedTool.value)
}
async function load() {
  const data = await listTools()
  tools.value = data.tools || []
  if (!selectedToolName.value && tools.value.length) selectedToolName.value = tools.value[0].name
  fillTemplate()
}
async function execute() {
  if (!selectedToolName.value) return
  loading.value = true
  try {
    const args = JSON.parse(argumentsText.value || '{}')
    const data = await executeTool({ toolName: selectedToolName.value, arguments: args })
    resultText.value = JSON.stringify(data, null, 2)
  } catch (e) {
    resultText.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
