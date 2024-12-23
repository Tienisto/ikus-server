import '@mdi/font/css/materialdesignicons.css'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import {defineNuxtPlugin} from "#app/nuxt";
import {de} from "vuetify/locale";
import { VTimePicker } from 'vuetify/labs/VTimePicker'

export default defineNuxtPlugin((app) => {
  const vuetify = createVuetify({
    components: {
      VTimePicker, // TODO: Remove when promoted to stable
    },
    locale: {
      locale: 'de',
      messages: { de },
    },
    theme: {
      themes: {
        light: {
          dark: false,
          colors: {
            primary: '#7a003f',
            secondary: '#eaeaea',
            accent: '#3d0020',
            error: '#b71c1c',
          },
        },
      },
    },
  })
  app.vueApp.use(vuetify)
})
