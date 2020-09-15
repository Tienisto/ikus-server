import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from './views/Login'
import Users from './views/Users'
import Logs from "@/views/Logs";
import Channels from "@/views/Channels";
import Dashboard from "@/views/Dashboard";
import Posts from "@/views/Posts";
import Calendar from "@/views/Calendar";
import Links from "@/views/Links";
import Handbook from "@/views/Handbook";
import FAQ from "@/views/FAQ";
import Contact from "@/views/Contact";
import Statistics from "@/views/Statistics";
import Status from "@/views/Status";
import Privacy from "@/views/Privacy";

Vue.use(VueRouter)

  const routes = [
  {
    path: '/',
    name: 'Home',
    component: Login
  },
  {
    path: '/status',
    name: 'Status',
    component: Status
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: Privacy
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
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/posts',
    name: 'Posts',
    component: Posts
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: Calendar
  },
  {
    path: '/links',
    name: 'Links',
    component: Links
  },
  {
    path: '/handbook',
    name: 'Handbook',
    component: Handbook
  },
  {
    path: '/faq',
    name: 'FAQ',
    component: FAQ
  },
  {
    path: '/contact',
    name: 'Contact',
    component: Contact
  },
  {
    path: '/channels',
    name: 'Channels',
    component: Channels
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
