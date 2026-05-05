<template>
  <div class="console-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-icon"><Sparkles :size="17" /></div>
        <div>
          <div class="brand-title">SmartKB</div>
          <div class="brand-subtitle">AI Console</div>
        </div>
      </div>

      <div class="nav-section-title">API 平台</div>
      <nav class="nav-list">
        <RouterLink class="nav-item" to="/applications"><Bot :size="16" />应用</RouterLink>
        <RouterLink class="nav-item" to="/knowledge"><Database :size="16" />知识库</RouterLink>
        <RouterLink class="nav-item" to="/models"><Cpu :size="16" />模型</RouterLink>
      </nav>

      <div class="nav-section-title">智能体</div>
      <nav class="nav-list">
        <RouterLink class="nav-item" to="/agent"><Workflow :size="16" />Agent 执行台</RouterLink>
        <RouterLink class="nav-item" to="/tools"><Wrench :size="16" />工具调试台</RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div style="display:flex;align-items:center;gap:8px;"><CircleHelp :size="15" />帮助中心</div>
      </div>
    </aside>

    <main class="main-panel">
      <header class="page-header">
        <div>
          <div class="breadcrumb"><span>控制台</span><span>/</span><span>{{ route.meta.title }}</span></div>
          <h1 class="page-title">{{ route.meta.title }}</h1>
          <p class="page-subtitle">{{ route.meta.subtitle }}</p>
        </div>
        <div class="page-actions">
          <button v-if="route.path === '/applications'" class="btn btn-primary" @click="appBus.openCreateApp?.()">
            <Plus :size="15" />创建应用
          </button>
          <button v-if="route.path === '/knowledge'" class="btn btn-primary" @click="appBus.openCreateKb?.()">
            <Plus :size="15" />创建知识库
          </button>
        </div>
      </header>

      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { provide, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { Bot, CircleHelp, Cpu, Database, Plus, Sparkles, Workflow, Wrench } from 'lucide-vue-next'

const route = useRoute()
const appBus = reactive({})
provide('appBus', appBus)
</script>
