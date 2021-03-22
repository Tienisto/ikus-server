import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from './views/Login'
import Users from './views/Users'
import Channels from "@/views/Channels";
import Dashboard from "@/views/Dashboard";
import Posts from "@/views/Posts";
import Calendar from "@/views/Calendar";
import Links from "@/views/Links";
import Handbook from "@/views/Handbook";
import FAQ from "@/views/FAQ";
import Contacts from "@/views/Contacts";
import Statistics from "@/views/Statistics";
import Status from "@/views/Status";
import Privacy from "@/views/Privacy";
import Features from "@/views/Features";
import Activities from "@/views/Activities";
import SysLogs from "@/views/SysLogs";
import Audio from "@/views/Audio";
import EventRegistrations from "@/views/EventRegistrations";

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
    path: '/activities',
    name: 'Activities',
    component: Activities
  },
  {
    path: '/sys-logs',
    name: 'SysLogs',
    component: SysLogs
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
    path: '/calendar/registrations',
    name: 'EventRegistrations',
    component: EventRegistrations
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
    path: '/audio',
    name: 'Audio',
    component: Audio
  },
  {
    path: '/faq',
    name: 'FAQ',
    component: FAQ
  },
  {
    path: '/contacts',
    name: 'Contacts',
    component: Contacts
  },
  {
    path: '/features',
    name: 'Features',
    component: Features
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
