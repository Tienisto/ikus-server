import Vue from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify';
import moment from "moment";
import { Map, TileLayer, OsmSource } from 'vuelayers'
import 'vuelayers/lib/style.css'

Vue.config.productionTip = false;
moment.locale('de');

// vuelayers
Vue.use(Map);
Vue.use(TileLayer);
Vue.use(OsmSource);

new Vue({
  router,
  vuetify,
  render: h => h(App)
}).$mount('#app')
