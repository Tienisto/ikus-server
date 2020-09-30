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
        <v-card>
          <v-card-title>
            Sicherheit
          </v-card-title>
          <v-card-text>
            <StatusRow key-text="Admin-Passwort:" :value-ok="status.adminPassword" :value-error="!fetching && !status.adminPassword" error-text="Standard (!)"/>
            <StatusRow key-text="JWT (Webseite):"
                       :value-ok="status.jwtWebsite === 'OK'" :value-error="!fetching && status.jwtWebsite !== 'OK'"
                       :error-text="status.jwtWebsite === 'DEFAULT' ? 'Standard (!)' : 'zu kurz (min. 32)'"
            />
            <StatusRow key-text="JWT (App):"
                       :value-ok="status.jwtApp === 'OK'" :value-error="!fetching && status.jwtApp !== 'OK'"
                       :error-text="status.jwtApp === 'DEFAULT' ? 'Standard (!)' : 'zu kurz (min. 32)'"
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

    <v-card v-if="status.env" class="mt-4">
      <v-card-title>
        Systemvariablen
      </v-card-title>
      <v-card-text>
        Diese Informationen sind nur für Sie sichtbar.
        <br>
        Es wird nur ein Ausschnitt Ihnen angezeigt. <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html" target="_blank">Vollständige Liste</a>
        <br>
        Als Argument: <span style="font-family: Consolas, monospace">java -jar server.jar --storage.path="/ihr pfad/hier"</span>
        <br>
        Als Umgebungsvariable: <span style="font-family: Consolas, monospace">STORAGE_PATH=/ihr pfad/hier</span>
        <br><br>


        <v-data-table
            v-if="status.env"
            :loading="fetching"
            :headers="[
                { text: 'Variable', value: 'key' },
                { text: 'Beschreibung', value: 'description' },
                { text: 'Standard', value: 'default' },
                { text: 'Aktiv', value: 'value' }
            ]"
            :items="status.env"
            :items-per-page="-1"
            loading-text="Lade Daten..."
            no-data-text="Keine Moderatoren vorhanden"
            hide-default-footer
        >
        </v-data-table>
      </v-card-text>
    </v-card>

    <ToHomePageButton />

  </MainContainer>
</template>

<script>
import moment from "moment"
import MainContainer from "@/components/layout/MainContainer";
import StatusRow from "@/components/StatusRow";
import ToHomePageButton from "@/components/ToHomePageButton";
import {getStatus} from "@/api";

export default {
  name: 'StatusView',
  components: {ToHomePageButton, StatusRow, MainContainer},
  data: () => ({
    fetching: true,
    status: {},
    runTime: null,
    timerId: null
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
    this.status = await getStatus();
    this.fetching = false;

    this.runTime = this.status.runTime;
    this.timerId = setInterval(() => {
      this.runTime += 1000;
    }, 1000);
  },
  destroyed: function() {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }
}
</script>