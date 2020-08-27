import Vue from 'vue';
import Vuetify from 'vuetify/lib';

Vue.use(Vuetify);

export default new Vuetify({
    theme: {
        themes: {
            light: {
                primary: '#7a003f',
                secondary: '#e0e2e3',
                accent: '#3d0020',
                error: '#b71c1c',
            },
        },
    },
});
