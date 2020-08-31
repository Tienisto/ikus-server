import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from './views/Login'
import Users from './views/Users'
import Logs from "@/views/Logs";

Vue.use(VueRouter)

  const routes = [
  {
    path: '/',
    name: 'Home',
    component: Login
  },
  {
    path: '/users',
    name: 'Users',
    component: Users
  },
  {
    path: '/logs',
    name: 'Logs',
    component: Logs
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
