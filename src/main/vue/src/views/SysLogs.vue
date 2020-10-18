<template>
  <MainContainer title="System-Logs" icon="mdi-text-subject" :max-width="1600" :top-spacer="false">

    <template v-slot:intro>
      Die Logdaten sind nur für Sie sichtbar.
    </template>

    <LoadingIndicator v-if="fetching" />
    <Notice v-else-if="error" title="Ein Fehler ist aufgetreten." />
    <v-card v-else>
      <v-card-text style="font-family: Consolas,monospace; white-space: pre-wrap; font-size: 12px">{{ logs }}</v-card-text>
    </v-card>

  </MainContainer>
</template>

<script>
import {getSysLogs} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import LoadingIndicator from "@/components/LoadingIndicator";
import Notice from "@/components/Notice";

export default {
  name: 'SysLogsView',
  components: {Notice, LoadingIndicator, MainContainer},
  data: () => ({
    fetching: true,
    logs: '',
    timerId: null,
    error: false
  }),
  methods: {
    fetchData: async function() {
      try {
        const response = await getSysLogs();
        if (this.logs !== response.logs)
          this.logs = response.logs;
      } catch (e) {
        this.error = true;
        if (this.timerId) {
          clearInterval(this.timerId);
        }
      } finally {
        this.fetching = false;
      }
    },
  },
  mounted: async function() {
    await this.fetchData();
    this.timerId = setInterval(() => {
      this.fetchData();
    }, 3000);
  },
  destroyed: function() {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }
}
</script>