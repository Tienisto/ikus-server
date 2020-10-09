<template>
  <nav>
    <v-app-bar v-if="loggedIn" class="hidden-lg-and-up primary" dark>
      <v-app-bar-nav-icon @click="drawerVisible = true"/>
      <v-toolbar-title>
        Welcome to OVGU
      </v-toolbar-title>
    </v-app-bar>
    <v-navigation-drawer v-if="loggedIn" v-model="drawerVisible" class="primary" dark app>

      <v-container id="nav-logo-small" class="white--text pt-6 pl-4 pb-2" style="display: flex; align-content: center;">
        <img src="@/assets/logo-512-alpha.png" style="width: 70px; height: 70px">
        <div class="pl-2" style="display: flex; flex-direction: column; justify-content: center;">
          <p class="text-body-1 font-weight-bold mb-0">Welcome to OVGU</p>
          <p class="mb-0">Administration</p>
        </div>
      </v-container>
      <v-container id="nav-logo-big" class="white--text mt-4" style="display: none; text-align: center">
        <img src="@/assets/logo-512-alpha.png" style="width: 100px">
        <br>
        <p class="text-h5 mb-0">Welcome to OVGU</p>
        <p class="text-h7">Administration</p>
      </v-container>

      <v-list shaped dense>

        <template v-if="!admin">

          <v-list-item class="mb-3" link to="/dashboard">
            <v-list-item-icon>
              <v-icon>mdi-view-dashboard</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>Dashboard</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider/>
          <v-subheader class="ml-2">Inhalt</v-subheader>

          <v-list-item v-for="p in contentPages" :key="p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider/>
          <v-subheader class="ml-2">Analysis</v-subheader>

          <v-list-item v-for="p in analysisPages" :key="p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider/>
        </template>

        <template v-if="admin">

          <v-list-item link to="/users">
            <v-list-item-icon>
              <v-icon>mdi-account-multiple</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>Moderatoren</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider/>
          <v-subheader class="ml-2">Analysis</v-subheader>

          <v-list-item v-for="p in adminAnalysisPages" :key="p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>

        <v-list-item link class="mt-4 white" @click="logout" light :disabled="loggingOut">
          <v-list-item-icon>
            <v-icon>mdi-logout</v-icon>
          </v-list-item-icon>

          <v-list-item-content>
            <v-list-item-title>{{ loggingOut ? 'Wird abgemeldet...' : 'Abmelden' }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>

    </v-navigation-drawer>
  </nav>
</template>

<script>

import {logout} from "@/api";

export default {
  name: 'Navigation',
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
        route: '/faq',
        label: 'FAQ',
        icon: 'mdi-help-circle'
      },
      {
        route: '/contacts',
        label: 'Kontakte',
        icon: 'mdi-card-account-mail'
      },
    ],
    analysisPages: [
      {
        route: '/statistics',
        label: 'Statistiken',
        icon: 'mdi-finance'
      },
      {
        route: '/logs',
        label: 'Logs',
        icon: 'mdi-text-subject'
      }
    ],
    adminAnalysisPages: [
      {
        route: '/statistics',
        label: 'Statistiken',
        icon: 'mdi-finance'
      },
      {
        route: '/logs',
        label: 'Logs',
        icon: 'mdi-text-subject'
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
      await this.$emit('update-login');
      await this.$router.push('/');
    }
  },
  watch: {
    loggedIn: {
      immediate: true,
      handler: function (val) {
        if (this.drawerVisible) {
          if (!val) {
            this.drawerVisible = false; // loggedIn -> logout
          }
        } else {
          this.drawerVisible = val ? null : false; // initial
        }
      }
    }
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