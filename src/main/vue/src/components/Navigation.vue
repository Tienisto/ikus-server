<template>
  <nav>
    <v-app-bar v-if="loggedIn" class="hidden-lg-and-up primary" dark>
      <v-app-bar-nav-icon @click="drawerVisible = true"/>
      <v-toolbar-title>
        App - Administration
      </v-toolbar-title>
    </v-app-bar>
    <v-navigation-drawer v-if="loggedIn" v-model="drawerVisible" class="primary" dark app>
      <v-container class="white--text" style="text-align: center">
        <img src="@/assets/logo-512-alpha.png" class="mt-4" style="width: 100px">
        <br>
        <p class="text-h5 mb-0">IKUS - App</p>
        <p class="text-h7">Administration</p>
      </v-container>

      <v-list shaped dense>

        <template v-if="!admin">
          <v-divider/>
          <v-subheader class="ml-2 mt-1">Inhalt</v-subheader>

          <v-list-item v-for="p in contentPages" :key="p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider/>

          <v-subheader class="ml-2 mt-1">Analysis</v-subheader>

          <v-list-item v-for="p in analysisPages" :key="p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>

        <template v-if="admin">
          <v-divider/>

          <v-list-item v-for="p in adminPages" :key="'a-'+p.route" link :to="p.route">
            <v-list-item-icon>
              <v-icon>{{ p.icon }}</v-icon>
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ p.label }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>

        <v-divider/>

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
        route: '/dashboard',
        label: 'Dashboard',
        icon: 'mdi-view-dashboard'
      },
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
        route: '/contact',
        label: 'Kontakt',
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
    adminPages: [
      {
        route: '/users',
        label: 'Moderatoren',
        icon: 'mdi-account-multiple'
      },
      {
        route: '/logs',
        label: 'Logs',
        icon: 'mdi-text-subject'
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