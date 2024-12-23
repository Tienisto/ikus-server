<template>
  <v-app-bar v-if="loggedIn && isLgOrSmaller" class="bg-primary" dark>
    <v-app-bar-nav-icon @click="drawerVisible = !drawerVisible"/>
    <v-toolbar-title>
      Welcome to OVGU
    </v-toolbar-title>
  </v-app-bar>
  <v-navigation-drawer v-if="loggedIn" v-model="drawerVisible" class="bg-primary" dark app>

    <v-container id="nav-logo-small" class="text-white pt-6 pl-4 pb-2" style="display: flex; align-content: center;">
      <img src="@/assets/logo-512-alpha.png" style="width: 70px; height: 70px" alt="logo">
      <div class="pl-2" style="display: flex; flex-direction: column; justify-content: center;">
        <p class="text-body-1 font-weight-bold mb-0">Welcome to OVGU</p>
        <p class="mb-0">Administration</p>
      </div>
    </v-container>
    <v-container id="nav-logo-big" class="mt-4" style="display: none; text-align: center">
      <img src="@/assets/logo-512-alpha.png" style="width: 100px" alt="logo">
      <br>
      <p class="text-h5 mb-0">Welcome to OVGU</p>
      <p class="text-h7">Administration</p>
    </v-container>

    <v-list density="compact" nav>

      <template v-if="!admin">
        <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" class="mb-3" to="/dashboard"/>

        <v-divider/>
        <v-list-item subtitle="Inhalt"/>

        <v-list-item v-for="p in contentPages" :key="p.route"
                     :to="p.route" :prepend-icon="p.icon" :title="p.label"/>

        <v-divider/>
        <v-list-item subtitle="Analysis"/>

        <v-list-item v-for="p in analysisPages" :key="p.route" :to="p.route" :prepend-icon="p.icon"
                     :title="p.label"/>

        <v-divider/>
      </template>

      <template v-if="admin">

        <v-list-item prepend-icon="mdi-account-multiple" title="Moderatoren" to="/users"/>

        <v-divider/>
        <v-list-item class="ml-2" subtitle="Analysis"/>

        <v-list-item v-for="p in adminAnalysisPages" :key="p.route" :to="p.route" :prepend-icon="p.icon"
                     :title="p.label"/>

        <v-divider/>
      </template>

      <v-list-item class="mt-6 white" @click="logout" :disabled="loggingOut"
                   variant="flat"
                   prepend-icon="mdi-logout" title="Abmelden"/>
    </v-list>

  </v-navigation-drawer>
</template>

<script lang="ts">
import {logout} from "@/api";
import { breakpointsVuetifyV3, useBreakpoints } from '@vueuse/core'

export default {
  props: ['loggedIn', 'admin'],
  data: () => ({
    loggingOut: false,
    drawerVisible: false,
    contentPages: [
      {
        route: '/posts',
        label: 'Posts',
        icon: 'mdi-pencil'
      },
      {
        route: '/calendar',
        label: 'Kalender',
        icon: 'mdi-calendar'
      },
      {
        route: '/channels',
        label: 'Kanäle',
        icon: 'mdi-filter'
      },
      {
        route: '/links',
        label: 'Links',
        icon: 'mdi-web'
      },
      {
        route: '/handbook',
        label: 'Handbook',
        icon: 'mdi-file-document'
      },
      {
        route: '/audio',
        label: 'Guides',
        icon: 'mdi-headphones'
      },
      {
        route: '/faq',
        label: 'FAQ',
        icon: 'mdi-help-circle'
      },
      {
        route: '/contacts',
        label: 'Kontakte',
        icon: 'mdi-card-account-mail'
      },
      {
        route: '/features',
        label: 'Features',
        icon: 'mdi-format-list-bulleted-square'
      }
    ],
    analysisPages: [
      {
        route: '/statistics',
        label: 'Statistiken',
        icon: 'mdi-finance'
      },
      {
        route: '/activities',
        label: 'Aktivitäten',
        icon: 'mdi-history'
      }
    ],
    adminAnalysisPages: [
      {
        route: '/statistics',
        label: 'Statistiken',
        icon: 'mdi-finance'
      },
      {
        route: '/activities',
        label: 'Aktivitäten',
        icon: 'mdi-history'
      },
      {
        route: '/sys-logs',
        label: 'System-Logs',
        icon: 'mdi-text-long'
      },
      {
        route: '/status',
        label: 'Status',
        icon: 'mdi-information'
      }
    ]
  }),
  methods: {
    logout: async function () {
      this.loggingOut = true;
      await logout();
      this.loggingOut = false;
      this.$emit('update-login');
      await this.$router.push('/');
    }
  },
  watch: {
    loggedIn: {
      immediate: true,
      handler(val) {
        if (this.drawerVisible) {
          if (!val) {
            this.drawerVisible = false; // loggedIn -> logout
          }
        } else {
          this.drawerVisible = !!val; // initial
        }
      }
    }
  },
  setup() {
    const breakpoints = useBreakpoints(breakpointsVuetifyV3);
    const isLgOrSmaller = breakpoints.smallerOrEqual('lg');
    return {isLgOrSmaller};
  }
}
</script>

<style>
@media screen and (min-width: 1500px) {
  #nav-logo-small {
    display: none !important;
  }

  #nav-logo-big {
    display: block !important;
  }
}
</style>
