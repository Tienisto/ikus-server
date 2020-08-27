<template>
  <v-app>
    <Navigation :logged-in="!!user" @update-login="updateLogin" />
    <v-main>
      <router-view @update-login="updateLogin" />
    </v-main>
  </v-app>
</template>

<script>
import Navigation from "@/components/Navigation";
import {getUserInfo, initAccountInfo} from "@/api";

export default {
  name: 'App',
  components: { Navigation },
  data: () => ({
    user: null,
  }),
  methods: {
    updateLogin: function() {
      this.user = getUserInfo();
    }
  },
  mounted: async function() {
    await initAccountInfo();
    this.user = getUserInfo();
    if (this.$router.currentRoute.path === '/' && this.user) {
      // redirect to dashboard if already logged in
      await this.$router.push('/dashboard').catch(() => {});
    }
  }
};
</script>
