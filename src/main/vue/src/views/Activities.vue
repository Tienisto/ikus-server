<template>
  <MainContainer title="Aktivitäten" icon="mdi-history">

    <template v-slot:intro>
      Alle Nutzer können einsehen, was im System passiert.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Limit</p>

      <v-btn
          :class="limit === l ? ['primary'] : ['secondary', 'black--text']" class="ml-1 mr-1"
          @click="updateLimit(l)" v-for="l in limits" :key="'limit-'+l">
        {{ l }}
      </v-btn>
    </template>

    <v-card>
      <v-card-text>
        <v-data-table
            :loading="fetching"
            :headers="[
                { text: 'Aktion', value: 'type', width: '200px' },
                { text: 'Von', value: 'user', width: '100px' },
                { text: 'Info', value: 'info' },
                { text: 'Zeitstempel', value: 'timestamp', width: '155px' }
            ]"
            :items="logs"
            :items-per-page="-1"
            loading-text="Lade Daten..."
            no-data-text="Es ist noch nichts passiert"
            hide-default-footer
        >

          <template v-slot:item.type="props">
            {{ typeString(props.item.type) }}
          </template>

          <template v-slot:item.user="props">
            {{ props.item.user ? props.item.user.name : '-' }}
          </template>

          <template v-slot:item.info="props">
            {{ props.item.info }}
          </template>

          <template v-slot:item.timestamp="props">
            {{ timeString(props.item.timestamp) }}
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

  </MainContainer>
</template>

<script>
import moment from "moment"
import {getLogs} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import {logTypeString} from "@/utils";

export default {
  name: 'LogsView',
  components: {MainContainer},
  data: () => ({
    fetching: true,
    logs: [],
    limit: 50,
    limits: [50, 100, 500]
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.logs = await getLogs({ limit: this.limit });
      this.fetching = false;
    },
    updateLimit: async function(newLimit) {
      this.limit = newLimit;
      await this.fetchData();
    }
  },
  computed: {
    typeString: function() {
      return logTypeString;
    },
    timeString: function() {
      return (time) => moment(time).format('DD.MM.YYYY, HH:mm');
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>