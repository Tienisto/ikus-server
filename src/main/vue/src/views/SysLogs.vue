<template>
  <MainContainer title="System-Logs" icon="mdi-text-subject" :max-width="1600">

    <template v-slot:intro>
      Die Logdaten sind nur für Sie sichtbar.
    </template>

    <v-card>
      <v-card-text style="font-family: Consolas,monospace; white-space: pre-wrap; font-size: 12px">{{ logs }}</v-card-text>
    </v-card>

  </MainContainer>
</template>

<script>
import {getSysLogs} from "@/api";
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'SysLogsView',
  components: {MainContainer},
  data: () => ({
    fetching: true,
    logs: '',
    timerId: null
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      const response = await getSysLogs();
      if (this.logs !== response.logs)
        this.logs = response.logs;
      this.fetching = false;
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