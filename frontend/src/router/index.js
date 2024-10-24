// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import QAndA from '../components/QAndA.vue';  // 导入新页面组件
import HelloWorld from '../components/HelloWorld.vue';  // 其他组件

const routes = [
    {
        path: '/',
        name: 'QAndA',
        component: QAndA,
    },
    {
        path: '/hello',
        name: 'HelloWorld',
        component: HelloWorld,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
