<template>
  <MainContainer title="Aktivitäten" icon="mdi-history">

    <template v-slot:intro>
      Alle Nutzer können einsehen, was im System passiert.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Limit</p>

      <v-btn
          :class="limit === l ? ['bg-primary'] : ['bg-grey-lighten-3']" class="ml-1 mr-1" variant="text"
          @click="updateLimit(l)" v-for="l in limits" :key="'limit-'+l">
        {{ l }}
      </v-btn>
    </template>

    <v-card>
      <v-card-text>
        <v-data-table
            :loading="fetching"
            :headers="[
                { title: 'Aktion', key: 'type', width: '200px', value: item => typeString(item.type) },
                { title: 'Von', key: 'user', width: '100px', value: item => item.user ? item.user.name : '-' },
                { title: 'Info', key: 'info', value: item => item.info },
                { title: 'Zeitstempel', key: 'timestamp', width: '155px', value: item => timeString(item.timestamp) }
            ]"
            :items="logs"
            :items-per-page="-1"
            loading-text="Lade Daten..."
            no-data-text="Es ist noch nichts passiert"
            hide-default-footer
        >
        </v-data-table>
      </v-card-text>
    </v-card>

  </MainContainer>
</template>

<script lang="ts">
import moment from "moment"
import {getLogs} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import {logTypeString} from "~/utils/logType";
import type {LogDto} from "~/models";

export default {
  name: 'LogsView',
  components: {MainContainer},
  data: () => ({
    fetching: true,
    logs: [] as LogDto[],
    limit: 50,
    limits: [50, 100, 500]
  }),
  methods: {
    fetchData: async function () {
      this.fetching = true;
      this.logs = await getLogs({limit: this.limit});
      this.fetching = false;
    },
    updateLimit: async function (newLimit) {
      this.limit = newLimit;
      await this.fetchData();
    }
  },
  computed: {
    typeString: function () {
      return logTypeString;
    },
    timeString: function () {
      return (time: string) => moment(time).format('DD.MM.YYYY, HH:mm');
    }
  },
  mounted: async function () {
    await this.fetchData();
  }
}
</script>
