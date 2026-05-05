<template>
  <section class="card" style="padding: 40px; min-height: 520px; display: flex; align-items: center; justify-content: center; text-align: center;">
    <div>
      <div class="entity-icon entity-icon-blue" style="margin: 0 auto 12px;"><Cpu :size="18" /></div>
      <div class="section-title" style="justify-content:center; margin-bottom: 10px;">模型配置即将上线</div>
      <div style="font-size:13px; color: var(--text-tertiary);">当前版本专注于应用管理、知识库 RAG、Agent 路由和工具执行链路。</div>
    </div>
  </section>
</template>

<script setup>
import { Cpu } from 'lucide-vue-next'
</script>
