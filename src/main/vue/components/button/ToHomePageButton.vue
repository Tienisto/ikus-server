<template>
  <router-link v-if="!loggedIn" to="/" v-slot="{ href, navigate }" class="mt-4" custom>
    <v-btn @click="navigate" :href="href" prepend-icon="mdi-arrow-left" color="black" variant="text">
      Startseite
    </v-btn>
  </router-link>
</template>

<script lang="ts">
import {sleep} from "~/utils/sleep";
import {getUserInfo, isInitialized} from "~/api";

export default {
  name: 'ToHomePageButton',
  components: {},
  data: () => ({
    fetching: true,
    loggedIn: true
  }),
  mounted: async function () {
    while (!isInitialized()) {
      await sleep(200);
    }
    this.loggedIn = !!getUserInfo();
    this.fetching = false;
  }
}
</script>
