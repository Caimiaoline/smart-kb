<template>
  <section class="card" style="padding: 18px;">
    <div class="toolbar">
      <div class="section-title">我的应用 <span class="badge badge-gray">{{ apps.length }} 个</span></div>
      <button class="btn" @click="load"><RefreshCcw :size="14" />刷新</button>
    </div>

    <div v-if="!apps.length" class="empty-state">
      <Bot :size="32" />
      <div style="margin-top: 10px; font-weight: 650; color: var(--text-main);">还没有任何应用</div>
      <div style="margin-top: 4px; font-size: 13px;">点击右上角「创建应用」开始配置第一个智能体。</div>
    </div>

    <div v-else class="grid-cards">
      <article v-for="app in apps" :key="app.id" class="card card-hover entity-card">
        <div class="entity-card-head">
          <div class="entity-card-main">
            <div class="entity-icon"><Bot :size="17" /></div>
            <div style="min-width:0;">
              <div class="entity-title-row">
                <h3 class="entity-title">{{ app.name || '未命名应用' }}</h3>
                <span :class="['badge', app.status ? '' : 'badge-gray']">{{ app.status ? '已发布' : '草稿' }}</span>
              </div>
              <div class="entity-id">ID&nbsp;&nbsp;{{ app.id || '-' }}</div>
            </div>
          </div>
          <span :style="{ color: app.status ? '#00a870' : 'var(--text-tertiary)' }" style="font-size: 12px; display:flex; align-items:center; gap:5px;">
            <span :style="{ background: app.status ? '#00b42a' : '#c9cdd4' }" style="width:7px;height:7px;border-radius:50%;"></span>
            {{ app.status ? '运行中' : '未启用' }}
          </span>
        </div>
        <p class="entity-desc">{{ app.description || '暂无描述' }}</p>
        <div class="stats-row">
          <div><div class="stat-value">Admin</div><div class="stat-label">创建人</div></div>
          <div><div class="stat-value">{{ app.boundKbIds?.length || 0 }}</div><div class="stat-label">关联知识库</div></div>
          <div><div class="stat-value">{{ app.status ? '已发布' : '草稿' }}</div><div class="stat-label">状态</div></div>
        </div>
        <div class="card-footer" style="justify-content:flex-end;">
          <button class="btn"><Settings2 :size="14" />配置</button>
          <button class="btn btn-primary"><MessagesSquare :size="14" />对话</button>
        </div>
      </article>
    </div>
  </section>

  <div v-if="showCreate" class="modal-mask">
    <div class="modal">
      <div class="modal-head">
        <strong>创建应用</strong>
        <button class="btn" @click="showCreate = false">关闭</button>
      </div>
      <form @submit.prevent="submit">
        <div class="modal-body">
          <div class="form-row"><label class="form-label">应用名称</label><input v-model="form.name" class="form-control" required /></div>
          <div class="form-row"><label class="form-label">图标</label><input v-model="form.icon" class="form-control" placeholder="bot" /></div>
          <div class="form-row"><label class="form-label">描述</label><textarea v-model="form.description" class="form-control" rows="3" /></div>
          <div class="form-row"><label class="form-label">系统 Prompt</label><textarea v-model="form.prompt" class="form-control" rows="4" /></div>
          <label style="display:flex;align-items:center;gap:8px;font-size:13px;"><input v-model="form.status" type="checkbox" /> 创建后发布</label>
        </div>
        <div class="modal-foot">
          <button type="button" class="btn" @click="showCreate = false">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">保存</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, reactive, ref } from 'vue'
import { Bot, MessagesSquare, RefreshCcw, Settings2 } from 'lucide-vue-next'
import { createApplication, listApplications } from '../api'

const appBus = inject('appBus')
const apps = ref([])
const showCreate = ref(false)
const saving = ref(false)
const form = reactive({ name: '', icon: 'bot', description: '', prompt: '', status: true })

appBus.openCreateApp = () => { showCreate.value = true }

async function load() {
  apps.value = await listApplications()
}

async function submit() {
  saving.value = true
  try {
    await createApplication({ ...form })
    showCreate.value = false
    Object.assign(form, { name: '', icon: 'bot', description: '', prompt: '', status: true })
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
