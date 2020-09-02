<template>
  <MainContainer title="Logs" icon="mdi-text-subject">

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
        <v-simple-table>
          <thead>
          <tr>
            <th class="text-left">Aktion</th>
            <th class="text-left">Von</th>
            <th class="text-left">Info</th>
            <th class="text-left">Zeitstempel</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(l, index) in logs" :key="'log-'+index">
            <td>{{ typeString(l.type) }}</td>
            <td>{{ l.user ? l.user.name : '-' }}</td>
            <td>{{ l.info }}</td>
            <td>{{ timestamp(l.timestamp) }}</td>
          </tr>
          </tbody>
        </v-simple-table>
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
    logs: [],
    limit: 50,
    limits: [50, 100, 500]
  }),
  methods: {
    fetchData: async function() {
      this.logs = (await getLogs({ limit: this.limit })).data;
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
    timestamp: function() {
      return (time) => moment(time).format('DD.MM.YYYY, HH:mm');
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>