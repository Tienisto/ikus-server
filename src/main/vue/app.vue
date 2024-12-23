<template>
  <NuxtLayout>
    <v-app>
      <NavigationSide :logged-in="!!user" :admin="user && user.admin" @update-login="updateLogin"/>

      <v-main class="bg-secondary">
        <NuxtPage @update-login="updateLogin"/>
      </v-main>

      <v-snackbar v-model="snackbar" location="bottom right">
        {{ snackbarText }}
        <template v-slot:actions>
          <v-btn
              color="pink"
              variant="text"
              @click="snackbar = false"
          >
            Schließen
          </v-btn>
        </template>
      </v-snackbar>
    </v-app>
  </NuxtLayout>
</template>

<script lang="ts">
import {initSnackbar} from "~/utils/snackbar";
import {getUserInfo, initAccountInfo, initAPI} from "~/api";
import type {MeDto} from "~/models";
import moment from "moment";
import "moment/dist/locale/de";
import NavigationSide from "~/components/NavigationSide.vue";

export default {
  components: {NavigationSide},
  data() {
    return {
      user: null as MeDto | null,
      snackbar: false,
      snackbarText: null as string | null,
    }
  },
  methods: {
    updateLogin() {
      this.user = getUserInfo();
    },
    async handle401() {
      this.updateLogin();
      await this.$router.push('/').catch(() => {
      });
    },
    showSnackbar(text: string) {
      this.snackbarText = text;
      this.snackbar = true;
    }
  },
  async mounted() {
    moment.locale('de');
    initSnackbar(this.showSnackbar);
    await initAccountInfo();
    initAPI(this.handle401);
    this.user = getUserInfo();
  },
}
</script>
