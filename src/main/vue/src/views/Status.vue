<template>
  <MainContainer title="Statusbericht" icon="mdi-information">

    <v-row>
      <v-col cols="12" sm="6" md="4">
        <v-card>
          <v-card-title>
            Komponenten
          </v-card-title>
          <v-card-text>
            <StatusRow key-text="Datenbank:" :value-ok="status.database" :value-error="!fetching && !status.database"/>
            <StatusRow key-text="Speicher (lesen):" :value-ok="status.storageRead" :value-error="!fetching && !status.storageRead"/>
            <StatusRow key-text="Speicher (schreiben):" :value-ok="status.storageRead" :value-error="!fetching && !status.storageRead"/>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="4">
        <v-card style="height: 100%">
          <v-card-title>
            Sicherheit
          </v-card-title>
          <v-card-text>
            <StatusRow key-text="Admin-Passwort:" :value-ok="status.adminPassword" :value-error="!fetching && !status.adminPassword" error-text="Standard (!)"/>
            <StatusRow key-text="JWT:"
                       :value-ok="status.jwt === 'OK'" :value-error="!fetching && status.jwt !== 'OK'"
                       :error-text="status.jwt === 'DEFAULT' ? 'Standard (!)' : 'zu kurz (min. 32)'"
            />
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="4">
        <v-card>
          <v-card-title>
            Über die Instanz
          </v-card-title>
          <v-card-text>
            <StatusRow key-text="Version:" :value-text="status.version"/>
            <StatusRow key-text="Kompiliert am:" :value-text="dateString"/>
            <StatusRow key-text="Laufzeit:" :value-text="runTimeString"/>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <router-link v-if="!loggedIn" to="/" v-slot="{ href, navigate }" class="mt-4">
      <v-btn @click="navigate" :href="href" color="black" text>
        <v-icon left>mdi-arrow-left</v-icon>
        Startseite
      </v-btn>
    </router-link>

  </MainContainer>
</template>

<script>
import moment from "moment"
import MainContainer from "@/components/layout/MainContainer";
import {getStatus, getUserInfo, isInitialized} from "@/api";
import StatusRow from "@/components/StatusRow";

export default {
  name: 'StatusView',
  components: {StatusRow, MainContainer},
  data: () => ({
    fetching: true,
    loggedIn: true,
    status: {},
    runTime: null
  }),
  computed: {
    dateString() {
      if (this.status.date)
        return moment(this.status.date).format('DD.MM.YYYY')
      else
        return null;
    },
    runTimeString() {
      if (!this.runTime)
        return null;
      const seconds = Math.floor(this.runTime / 1000) % 60
      const minutes = Math.floor(this.runTime / (1000 * 60)) % 60
      const hours = Math.floor(this.runTime / (1000 * 60 * 60)) % 24
      const days = Math.floor(this.runTime / (1000 * 60 * 60 * 24))
      const daysString = days === 1 ? 'Tag' : 'Tage'
      return `${days} ${daysString}, ${this.n(hours)}:${this.n(minutes)}:${this.n(seconds)}`
    },
    n() {
      return (num) => num < 10 ? '0'+num : num;
    }
  },
  mounted: async function() {
    while(!isInitialized()) {
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
    this.loggedIn = !!getUserInfo();
    this.status = (await getStatus()).data;
    this.fetching = false;

    this.runTime = this.status.runTime;
    setInterval(() => {
      this.runTime += 1000;
    }, 1000);
  }
}
</script>