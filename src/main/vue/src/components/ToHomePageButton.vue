<template>
  <router-link v-if="!loggedIn" to="/" v-slot="{ href, navigate }" class="mt-4">
    <v-btn @click="navigate" :href="href" color="black" text>
      <v-icon left>mdi-arrow-left</v-icon>
      Startseite
    </v-btn>
  </router-link>
</template>

<script>
import {getUserInfo, isInitialized} from "@/api";

export default {
  name: 'ToHomePageButton',
  components: {},
  data: () => ({
    fetching: true,
    loggedIn: true
  }),
  mounted: async function() {
    while(!isInitialized()) {
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    this.loggedIn = !!getUserInfo();
    this.fetching = false;
  }
}
</script>