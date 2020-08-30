<template>
  <v-app>
    <Navigation :logged-in="!!user" :admin="user && user.admin" @update-login="updateLogin" />
    <v-main>
      <router-view @update-login="updateLogin" />
    </v-main>

    <v-snackbar v-model="snackbar" :bottom="true" :right="true">
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn
            color="pink"
            text
            v-bind="attrs"
            @click="snackbar = false"
        >
          Schließen
        </v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script>
import Navigation from "@/components/Navigation";
import {getUserInfo, initAccountInfo, initAPI} from "@/api";
import {initSnackbar} from "@/utils";

export default {
  name: 'App',
  components: { Navigation },
  data: () => ({
    user: null,
    snackbar: false,
    snackbarText: null
  }),
  methods: {
    updateLogin: function() {
      this.user = getUserInfo();
    },
    handle401: async function() {
      this.updateLogin();
      await this.$router.push('/').catch(() => {});
    },
    showSnackbar: function(text) {
      this.snackbarText = text;
      this.snackbar = true;
    }
  },
  mounted: async function() {
    initSnackbar(this.showSnackbar);
    initAPI({ handle401: this.handle401 });
    await initAccountInfo();
    this.user = getUserInfo();
  }
};
</script>
