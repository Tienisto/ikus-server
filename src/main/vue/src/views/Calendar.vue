<template>
  <MainContainer title="Kalender" icon="mdi-calendar">

    <template v-slot:intro>
      Hier können Sie den Kalender verwalten.
      <br>
      Alle Events sind einem Kanal zugeordnet.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <v-select
          label="Kanal" style="width: 250px"
          v-model="channel" @change="updateChannel"
          :items="channelsWithAll" item-text="name.en" item-value="id"
          return-object
      />

      <LocaleSelector v-model="locale"/>

      <br>

      <v-btn @click="showCreateEvent()" color="primary" block :disabled="loading || channels.length === 0">
        <v-icon left>mdi-plus</v-icon>
        Neues Event
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching" />

    <v-card v-if="!fetching">
      <v-card-text>
        <Calendar :events="events" :locale="locale" @click:date="showCreateEvent" @click:event="showUpdateEvent" :calendar-style="{'min-height': $vuetify.breakpoint.lgAndUp ? '500px' : '400px'}">
          <template v-slot:header="{ currMonth, prev, next }">
            <div style="display: flex; align-items: center; justify-content: space-between">
              <v-btn @click="prev()" color="black" text>
                <v-icon dark>mdi-chevron-left</v-icon>
              </v-btn>
              <span class="text-h6">{{ currMonth }}</span>
              <v-btn @click="next()" color="black" text>
                <v-icon dark>mdi-chevron-right</v-icon>
              </v-btn>
            </div>
            <br>
          </template>
        </Calendar>
      </v-card-text>
    </v-card>

    <EventDialog ref="eventDialog" v-model="dialogEvent" :channels="channels" :updating="dialogUpdating" :loading="loading"
                  @submit="submit" @delete="showDeleteDialog" @registrations="$router.push(`/calendar/registrations?eventId=${selectedEvent.id}`)"/>

    <GenericDialog v-model="dialogDelete" title="Event löschen">
      <template v-slot:content>
        Folgendes Event wird gelöscht:
        <br>
        <b>{{ nameOfEvent }} ({{ dateOfEvent }})</b>
        <br><br>
        Möchten Sie wirklich diesen Event löschen?
        <br>
        Dieser Vorgang ist nicht widerrufbar.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDelete = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deleteEvent" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script>
import moment from "moment"
import MainContainer from "@/components/layout/MainContainer";
import {
  createEvent,
  deleteEvent,
  getAllEvents,
  getChannels,
  getEventsByChannel,
  updateEvent,
} from "@/api";
import LocaleSelector from "@/components/LocaleSelector";
import Calendar from "@/components/Calendar";
import EventDialog from "@/components/dialog/EventDialog";
import LoadingIndicator from "@/components/LoadingIndicator";
import {showSnackbar} from "@/utils";
import GenericDialog from "@/components/dialog/GenericDialog";

const channelAll = { id: -1, name: { en: 'Alle', de: 'Alle' } };

export default {
  name: 'CalendarView',
  components: {GenericDialog, LoadingIndicator, EventDialog, Calendar, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channelsWithAll: [],
    channels: [],
    channel: {},
    locale: 'EN',
    events: [],
    dialogEvent: false,
    dialogUpdating: false,
    dialogDelete: false,
    selectedEvent: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;

      this.channels = await getChannels({ type: 'CALENDAR' });
      this.channelsWithAll = [ channelAll, ...this.channels ];
      if (!this.channel.id) {
        // init
        this.channel = channelAll;
      }

      if (this.channel.id === channelAll.id) {
        // fetch all channels
        this.events = await getAllEvents();
      } else {
        this.events = await getEventsByChannel({ channelId: this.channel.id });
      }

      this.fetching = false;
    },
    updateChannel: async function() {
      await this.fetchData();
    },
    showCreateEvent: function(date = new Date().toISOString().substring(0, 10)) {
      this.$refs.eventDialog.reset(this.channel, this.locale, date);
      this.dialogEvent = true;
      this.dialogUpdating = false;
    },
    showUpdateEvent: function(event) {
      this.selectedEvent = event;
      this.$refs.eventDialog.reset(event.channel, this.locale, null);
      this.$refs.eventDialog.load(event);
      this.dialogUpdating = true;
      this.dialogEvent = true;
    },
    showDeleteDialog: function() {
      this.dialogEvent = false;
      this.dialogDelete = true;
    },
    submit: async function(event) {
      if (this.dialogUpdating)
        await this.updateEvent(event);
      else
        await this.createEvent(event);
    },
    createEvent: async function(event) {
      try {
        this.loading = true;
        await createEvent(event);
        this.dialogEvent = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateEvent: async function(event) {
      try {
        this.loading = true;
        await updateEvent({
          id: this.selectedEvent.id,
          ...event
        });
        this.dialogEvent = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteEvent: async function() {
      try {
        this.loading = true;
        await deleteEvent({ id: this.selectedEvent.id });
        this.dialogDelete = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
  },
  computed: {
    nameOfEvent: function() {
      if (this.selectedEvent.name)
        return this.selectedEvent.name.en;
      else
        return '';
    },
    dateOfEvent: function() {
      if (this.selectedEvent.startTime)
        return moment(this.selectedEvent.startTime).format('DD.MM.YYYY');
      else
        return '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>