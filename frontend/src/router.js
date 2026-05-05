import { createRouter, createWebHistory } from 'vue-router'
import ApplicationsView from './views/ApplicationsView.vue'
import KnowledgeView from './views/KnowledgeView.vue'
import ModelsView from './views/ModelsView.vue'
import AgentView from './views/AgentView.vue'
import ToolsView from './views/ToolsView.vue'

const routes = [
  { path: '/', redirect: '/applications' },
  { path: '/applications', component: ApplicationsView, meta: { title: '应用中心', subtitle: '管理你的智能体应用，配置飞书接入和知识库。' } },
  { path: '/knowledge', component: KnowledgeView, meta: { title: '知识库管理', subtitle: '维护面向 RAG 的知识空间，上传文档并测试命中率。' } },
  { path: '/models', component: ModelsView, meta: { title: '模型管理', subtitle: '配置底层推理模型与路由策略。' } },
  { path: '/agent', component: AgentView, meta: { title: 'Agent 执行台', subtitle: '展示任务路由、工具选择、执行结果和 Trace。' } },
  { path: '/tools', component: ToolsView, meta: { title: '工具调试台', subtitle: '查看 Tool Schema，并直接执行工具进行调试。' } }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
