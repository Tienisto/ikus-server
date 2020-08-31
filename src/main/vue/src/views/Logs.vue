<template>
  <MainContainer title="Logs" icon="mdi-text-subject">

    <template v-slot:meta>
      <p class="text-h6">Limit</p>

      <v-btn
          :color="limit === l ? 'primary' : 'grey'" class="ml-1 mr-1" dark
          @click="updateLimit(l)" v-for="l in limits" :key="'limit-'+l">
        {{ l }}
      </v-btn>
    </template>

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
          <td>{{ l.user ? l.user.name : 'gelöscht' }}</td>
          <td>{{ l.info }}</td>
          <td>{{ timestamp(l.timestamp) }}</td>
        </tr>
      </tbody>
    </v-simple-table>

  </MainContainer>
</template>

<script>
import moment from "moment"
import {getLogs} from "@/api";
import MainContainer from "@/components/layout/MainContainer";

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
      return (type) => {
        switch(type) {
          case 'CREATE_USER': return 'Moderator/in erstellt';
          case 'UPDATE_USER_NAME': return 'Nutzername geändert';
          case 'UPDATE_USER_PASSWORD': return 'Nutzerpasswort geändert';
          case 'DELETE_USER': return 'Moderator/in gelöscht';
          case 'CREATE_CHANNEL': return 'Kanal erstellt';
          case 'UPDATE_CHANNEL': return 'Kanal umbenannt';
          case 'DELETE_CHANNEL': return 'Kanal gelöscht';
          case 'CREATE_POST': return 'Beitrag erstellt';
          case 'UPDATE_POST': return 'Beitrag geändert';
          case 'DELETE_POST': return 'Beitrag gelöscht';
          case 'CREATE_EVENT': return 'Event erstellt';
          case 'UPDATE_EVENT': return 'Event geändert';
          case 'DELETE_EVENT': return 'Event gelöscht';
          case 'CREATE_LINK_GROUP': return 'Linkgruppe erstellt';
          case 'UPDATE_LINK_GROUP': return 'Linkgruppe geändert';
          case 'DELETE_LINK_GROUP': return 'Linkgruppe gelöscht';
          case 'CREATE_LINK': return 'Link erstellt';
          case 'UPDATE_LINK': return 'Link geändert';
          case 'DELETE_LINK': return 'Link gelöscht';
          case 'UPDATE_HAND_BOOK': return 'Handbook geändert';
          case 'CREATE_CONTACT': return 'Neue Kontaktdaten';
          case 'UPDATE_CONTACT': return 'Kontaktdaten geändert';
          case 'DELETE_CONTACT': return 'Kontaktdaten gelöscht';
          default: return 'unbekannt';
        }
      }
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