import '@mdi/font/css/materialdesignicons.css';
import Vue from 'vue';
import Vuetify from 'vuetify/lib';

Vue.use(Vuetify);

export default new Vuetify({
    theme: {
        themes: {
            light: {
                primary: '#7a003f',
                secondary: '#eaeaea',
                accent: '#3d0020',
                error: '#b71c1c',
            },
        },
    },
    icons: {
        iconfont: 'mdi'
    }
});
