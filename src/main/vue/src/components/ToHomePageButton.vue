<template>
  <router-link v-if="!loggedIn" to="/" v-slot="{ href, navigate }" class="mt-4" custom>
    <v-btn @click="navigate" :href="href" color="black" text>
      <v-icon left>mdi-arrow-left</v-icon>
      Startseite
    </v-btn>
  </router-link>
</template>

<script>
import {getUserInfo, isInitialized} from "@/api";
import {sleep} from "@/utils";

export default {
  name: 'ToHomePageButton',
  components: {},
  data: () => ({
    fetching: true,
    loggedIn: true
  }),
  mounted: async function() {
    while(!isInitialized()) {
      await sleep(200);
    }
    this.loggedIn = !!getUserInfo();
    this.fetching = false;
  }
}
</script>