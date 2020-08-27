<template>
  <v-app>
    <Navigation :logged-in="true" @update-login="updateLogin" />
    <v-main>
      <router-view @update-login="updateLogin" />
    </v-main>
  </v-app>
</template>

<script>
import Navigation from "@/components/Navigation";
import {isLoggedIn} from "@/api";

export default {
  name: 'App',
  components: { Navigation },
  data: () => ({
    user: null,
    admin: false
  }),
  methods: {
    updateLogin: function() {
      this.loggedIn = isLoggedIn();
    }
  },
};
</script>

<style>
  .primary-color {
    background-color: #7a003f;
  }
</style>
