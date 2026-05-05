<template>
  <section class="card" style="padding: 18px;">
    <div class="toolbar">
      <div class="section-title">知识库 <span class="badge badge-gray">{{ bases.length }} 个</span></div>
      <div style="display:flex;gap:10px;align-items:center;">
        <input v-model="keyword" class="form-control" style="width:240px;" placeholder="搜索知识库" />
        <button class="btn" @click="load"><RefreshCcw :size="14" />刷新</button>
      </div>
    </div>

    <div v-if="!filteredBases.length" class="empty-state">
      <Database :size="32" />
      <div style="margin-top: 10px; font-weight: 650; color: var(--text-main);">暂无知识库</div>
      <div style="margin-top: 4px; font-size: 13px;">点击右上角「创建知识库」，创建一个用于 RAG 的知识空间。</div>
    </div>

    <div v-else class="grid-cards">
      <article v-for="kb in filteredBases" :key="kb.id" class="card card-hover entity-card">
        <div class="entity-card-head">
          <div class="entity-card-main">
            <div class="entity-icon entity-icon-blue"><Database :size="17" /></div>
            <div style="min-width:0;">
              <div class="entity-title-row">
                <h3 class="entity-title">{{ kb.name || '未命名知识库' }}</h3>
                <span class="badge">{{ kb.type === 'WEB' ? 'WEB 采集' : '文档库' }}</span>
              </div>
              <div class="entity-id">ID&nbsp;&nbsp;{{ kb.id || '-' }}</div>
            </div>
          </div>
          <button class="btn" @click="pickFile(kb.id)"><UploadCloud :size="14" />上传</button>
          <input :ref="el => fileInputs[kb.id] = el" type="file" style="display:none" @change="event => upload(kb.id, event)" />
        </div>
        <p class="entity-desc">{{ kb.description || '暂无描述' }}</p>
        <div class="stats-row">
          <div><div class="stat-value">{{ formatDate(kb.updatedAt) }}</div><div class="stat-label">更新时间</div></div>
          <div><div class="stat-value">{{ kb.documentCount ?? '-' }}</div><div class="stat-label">文件数量</div></div>
          <div><div class="stat-value">{{ kb.segmentCount ?? '-' }}</div><div class="stat-label">切片数量</div></div>
        </div>
        <div class="card-footer">
          <span style="color:var(--text-tertiary);font-size:12px;">权限：{{ kb.permission === 'admin' ? '仅管理员' : '普通成员' }}</span>
          <div style="display:flex;gap:8px;">
            <button class="btn" @click="openDocs(kb)"><Files :size="14" />文档</button>
            <button class="btn btn-primary" @click="openHitTest(kb)"><Radar :size="14" />测试</button>
          </div>
        </div>
      </article>
    </div>
  </section>

  <div v-if="showCreate" class="modal-mask">
    <div class="modal">
      <div class="modal-head"><strong>创建知识库</strong><button class="btn" @click="showCreate = false">关闭</button></div>
      <form @submit.prevent="submitCreate">
        <div class="modal-body">
          <div class="form-row"><label class="form-label">名称</label><input v-model="form.name" class="form-control" required /></div>
          <div class="form-row"><label class="form-label">描述</label><textarea v-model="form.description" class="form-control" rows="3" /></div>
          <div class="form-row"><label class="form-label">类型</label><select v-model="form.type" class="form-control"><option value="DOCUMENT">文档库</option><option value="WEB">WEB 采集</option></select></div>
          <div class="form-row"><label class="form-label">权限</label><select v-model="form.permission" class="form-control"><option value="member">普通成员</option><option value="admin">仅管理员</option></select></div>
        </div>
        <div class="modal-foot"><button type="button" class="btn" @click="showCreate = false">取消</button><button class="btn btn-primary" :disabled="saving">保存</button></div>
      </form>
    </div>
  </div>

  <div v-if="hitKb" class="modal-mask">
    <div class="modal" style="width:760px;">
      <div class="modal-head"><strong>命中率测试 - {{ hitKb.name }}</strong><button class="btn" @click="hitKb = null">关闭</button></div>
      <div class="modal-body">
        <div class="form-row"><textarea v-model="hitQuery" class="form-control" rows="3" placeholder="输入测试问题" /></div>
        <div style="display:flex;gap:10px;margin-bottom:14px;"><input v-model.number="topN" class="form-control" style="width:120px;" type="number" /><input v-model.number="threshold" class="form-control" style="width:160px;" type="number" step="0.01" /><button class="btn btn-primary" @click="runHitTest">开始测试</button></div>
        <pre class="json-box">{{ hitResult }}</pre>
      </div>
    </div>
  </div>

  <div v-if="docsKb" class="modal-mask">
    <div class="modal" style="width:720px;">
      <div class="modal-head"><strong>文档列表 - {{ docsKb.name }}</strong><button class="btn" @click="docsKb = null">关闭</button></div>
      <div class="modal-body">
        <div v-if="!docs.length" class="empty-state">暂无文档</div>
        <div v-for="doc in docs" :key="doc.id" class="trace-item">
          <div><strong>{{ doc.fileName || doc.name || `文档 #${doc.id}` }}</strong><div class="entity-id">status {{ doc.status || '-' }}</div></div>
          <span class="badge badge-gray">{{ doc.segmentCount ?? '-' }} chunks</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, reactive, ref } from 'vue'
import { Database, Files, Radar, RefreshCcw, UploadCloud } from 'lucide-vue-next'
import { createKnowledgeBase, hitTestKnowledge, listKnowledgeBases, listKnowledgeDocuments, uploadKnowledgeDocument } from '../api'

const appBus = inject('appBus')
const bases = ref([])
const keyword = ref('')
const showCreate = ref(false)
const saving = ref(false)
const form = reactive({ name: '', description: '', type: 'DOCUMENT', permission: 'member' })
const fileInputs = reactive({})
const hitKb = ref(null)
const hitQuery = ref('')
const topN = ref(5)
const threshold = ref(0.35)
const hitResult = ref('等待测试...')
const docsKb = ref(null)
const docs = ref([])

appBus.openCreateKb = () => { showCreate.value = true }
const filteredBases = computed(() => bases.value.filter(kb => !keyword.value || `${kb.name || ''}${kb.description || ''}`.includes(keyword.value)))

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10)
}
async function load() { bases.value = await listKnowledgeBases() }
async function submitCreate() {
  saving.value = true
  try {
    await createKnowledgeBase({ ...form })
    showCreate.value = false
    Object.assign(form, { name: '', description: '', type: 'DOCUMENT', permission: 'member' })
    await load()
  } finally { saving.value = false }
}
function pickFile(kbId) { fileInputs[kbId]?.click() }
async function upload(kbId, event) {
  const file = event.target.files?.[0]
  if (!file) return
  await uploadKnowledgeDocument(kbId, file)
  event.target.value = ''
  await load()
}
function openHitTest(kb) { hitKb.value = kb; hitQuery.value = ''; hitResult.value = '等待测试...' }
async function runHitTest() {
  const data = await hitTestKnowledge(hitKb.value.id, { queryText: hitQuery.value, topN: topN.value, threshold: threshold.value })
  hitResult.value = JSON.stringify(data, null, 2)
}
async function openDocs(kb) {
  docsKb.value = kb
  docs.value = await listKnowledgeDocuments(kb.id)
}

onMounted(load)
</script>
