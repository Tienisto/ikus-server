import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from './views/Login'
import Users from './views/Users'

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
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
