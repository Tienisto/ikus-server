<template>
  <MainContainer title="Event-Anmeldungen" icon="mdi-clipboard-text">

    <template v-slot:intro>
      <div class="text-h5">{{ event.name.de }}</div>
      <div class="text-subtitle-1">{{ event.name.en }}</div>
      <div class="mt-2" style="display: flex; align-items: center">
        <v-icon class="mr-2">mdi-clock-outline</v-icon>
        <span class="text-subtitle-1">{{ eventTime }}</span>
      </div>
      <div class="mt-1" style="display: flex; align-items: center">
        <v-icon class="mr-2">mdi-map-marker</v-icon>
        <span class="text-subtitle-1">{{ event.place || '(keine Angabe)' }}</span>
      </div>
    </template>

    <template v-slot:meta>
      <div class="text-h5" style="text-align: center" :class="event.registrationOpen ? ['text-green'] : []">
        Anmeldung ist
      </div>
      <div class="text-h5" style="text-align: center; font-weight: bold" :class="event.registrationOpen ? ['text-green'] : []">
        {{ event.registrationOpen ? 'offen' : 'geschlossen' }}
      </div>
      <v-btn v-if="event.registrationOpen" @click="closeRegistration" color="primary" class="mt-6" block prepend-icon="mdi-lock">
        Schließen
      </v-btn>
      <v-btn v-else @click="showRegistrationConfig" color="primary" class="mt-6" block prepend-icon="mdi-lock-open">
        Öffnen
      </v-btn>
      <br>
      <p class="text-h6">Plätze</p>
      <ul class="pl-3 mt-2">
        <li>Plätze: {{ event.registrationSlots }}</li>
        <li>Warteliste: {{ event.registrationSlotsWaiting }}</li>
      </ul>

      <br>

      <p class="text-h6">Erforderliche Felder</p>
      <ul class="pl-3 mt-2">
        <li v-for="f in event.registrationFields" :key="f">{{ fieldString(f) }}</li>
      </ul>

      <br>

      <v-btn @click="showExportDialog" color="primary" block prepend-icon="mdi-export-variant">
        Export
      </v-btn>
    </template>

    <v-card>
      <v-card-text>
        <v-data-table
            :headers="[
                { title: 'Platz', value: 'position', sortable: false },
                { title: 'Vorname', value: 'firstName', sortable: false },
                { title: 'Nachname', value: 'lastName', sortable: false },
                { title: 'E-Mail', value: 'email', sortable: false },
                { title: 'Aktionen', value: 'actions', width: 140, sortable: false },
            ]"
            :items="event.registrations"
            :items-per-page="-1"
            loading-text="Loading..."
            no-data-text="Keine Anmeldungen"
            hide-default-footer
        >
          <template v-slot:item.position="{ index }">
            <div :class="index >= event.registrationSlots ? ['text-red'] : []">
              {{ index + 1 }} / {{ event.registrationSlots }}
            </div>
          </template>
          <template v-slot:item.actions="{ item }">
            <v-btn @click="showRegistrationInfo(item)" icon="mdi-information" variant="text" />
            <v-btn @click="showKick(item)" icon="mdi-delete" variant="text" />
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <EventRegistrationConfigDialog ref="configDialog" v-model="dialogConfig" :loading="loading"
                                   @submit="openRegistration"/>
    <EventRegistrationInfoDialog v-model="dialogInfo" :data="selectedRegistration" />
    <EventRegistrationExportDialog ref="exportDialog" v-model="dialogExport" @submit="exportData" />

    <GenericDeleteDialog v-model="dialogDelete" :loading="loading" dialog-title="Anmeldung löschen"
                         @delete="kickRegistration">
      Folgende Anmeldung wird gelöscht:
      <br>
      <b>{{ selectedRegistration.firstName }} {{ selectedRegistration.lastName }}</b>
      <br><br>
      Möchten Sie wirklich diese Anmeldung löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>

    <router-link to="/calendar" v-slot="{ href, navigate }" custom>
      <v-btn @click="navigate" :href="href" class="mt-6" prepend-icon="mdi-arrow-left" variant="text">
        Zurück
      </v-btn>
    </router-link>

  </MainContainer>
</template>

<script lang="ts">
import {getEventsById, kickEventRegistration, updateEventRegistration} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import moment from "moment";
import EventRegistrationExportDialog from "@/components/dialog/EventRegistrationExportDialog.vue";
import EventRegistrationInfoDialog from "@/components/dialog/EventRegistrationInfoDialog.vue";
import GenericDeleteDialog from "@/components/dialog/GenericDeleteDialog.vue";
import EventRegistrationConfigDialog from "@/components/dialog/EventRegistrationConfigDialog.vue";
import {showSnackbar} from "@/utils/snackbar.js";
import {type ChannelDto, type EventDto, ExportType, type RegistrationData, RegistrationField} from "@/models";

export default {
  name: 'EventRegistrationsView',
  components: {
    EventRegistrationConfigDialog,
    GenericDeleteDialog, EventRegistrationInfoDialog, EventRegistrationExportDialog, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    event: {
      id: 0, channel: {} as ChannelDto, place: null, coords: null, startTime: '', endTime: '', name: {de: '', en: ''}, info: null,
      registrationFields: [], registrationSlots: 0, registrationSlotsWaiting: 0, registrationOpen: false, registrations: []
    } as EventDto,
    selectedRegistration: {
      timestamp: '',
      token: '',
    } as RegistrationData,
    dialogConfig: false,
    dialogInfo: false,
    dialogExport: false,
    dialogDelete: false,
    timerId: null as NodeJS.Timeout | null,
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      const eventId = parseInt(this.$route.query.eventId as string);
      this.event = await getEventsById({eventId});
      this.fetching = false;
    },
    showRegistrationInfo: function(registration: RegistrationData) {
      this.selectedRegistration = registration;
      this.dialogInfo = true;
    },
    showKick: function(registration: RegistrationData) {
      this.selectedRegistration = registration;
      this.dialogDelete = true;
    },
    kickRegistration: async function() {
      try {
        this.loading = true;
        await kickEventRegistration({ eventId: this.event.id, token: this.selectedRegistration.token });
        this.dialogDelete = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    showRegistrationConfig: function() {
      this.$refs.configDialog.reset();
      if (this.event.registrationSlots) {
        // was opened before
        this.$refs.configDialog.load(this.event);
      }
      this.dialogConfig = true;
    },
    openRegistration: async function({slots, slotsWaiting, fields}: {slots: number, slotsWaiting: number, fields: RegistrationField[]}) {
      try {
        this.loading = true;
        await updateEventRegistration({
          eventId: this.event.id,
          fields: fields,
          slots: slots,
          slotsWaiting: slotsWaiting,
          open: true
        });
        await this.fetchData();
        this.dialogConfig = false;
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    closeRegistration: async function() {
      try {
        this.loading = true;
        await updateEventRegistration({
          eventId: this.event.id,
          fields: this.event.registrationFields,
          slots: this.event.registrationSlots,
          slotsWaiting: this.event.registrationSlotsWaiting,
          open: false
        });
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    showExportDialog: function() {
      this.$refs.exportDialog.load(this.event);
      this.dialogExport = true;
    },
    exportData: function(fields: RegistrationField[], type: ExportType) {
      window.open(`/api/events/export/registrations?eventId=${this.event.id}&type=${type}&fields=${fields.join(',')}`, '_blank');
    }
  },
  computed: {
    eventTime: function() {
      if (!this.event.startTime && !this.event.endTime)
        return '(keine Angabe)';

      const start = moment(this.event.startTime);
      const end = this.event.endTime ? moment(this.event.endTime) : null;

      if (start && end) {
        if (start.date() === end.date() && start.month() === end.month() && start.year() === end.year()) {
          // same day
          return start.format('DD.MM.YYYY, HH:mm') + ' - ' + end.format('HH:mm');
        } else {
          // multiple days
          return start.format('DD.MM.YYYY, HH:mm') + ' - ' + end.format('DD.MM.YYYY, HH:mm');
        }
      } else {
        return start.format('DD.MM.YYYY, HH:mm');
      }
    },
    fieldString: function() {
      return (field: RegistrationField) => {
        switch(field) {
          case 'MATRICULATION_NUMBER': return 'Matrikel-Nr.';
          case 'FIRST_NAME': return 'Vorname';
          case 'LAST_NAME': return 'Nachname';
          case 'EMAIL': return 'E-Mail';
          case 'ADDRESS': return 'Adresse';
          case 'COUNTRY': return 'Heimatland';
          case 'DATE_OF_BIRTH': return 'Geburtsdatum';
          default: return '';
        }
      }
    }
  },
  mounted: async function() {
    await this.fetchData();
    this.timerId = setInterval(() => {
      this.fetchData();
    }, 3000);
  },
  beforeUnmount: function() {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }
}
</script>
