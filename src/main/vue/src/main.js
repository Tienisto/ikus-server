import Vue from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify';
import {initAPI} from "@/api";

Vue.config.productionTip = false;

initAPI({
  handle401: async function() {
    await router.push('/').catch(() => {});
  }
});

new Vue({
  router,
  vuetify,
  render: h => h(App)
}).$mount('#app')
