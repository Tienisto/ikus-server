<template>
  <MainContainer title="Kalender" icon="mdi-calendar">

    <template v-slot:intro>
      Hier können Sie den Kalender verwalten.
      <br>
      Alle Events sind einem Kanal zugeordnet.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>

      <v-select
          label="Kanal" style="width: 250px" variant="underlined"
          v-model="channel" @update:model-value="updateChannel"
          :items="channelsWithAll" item-title="name.en" item-value="id"
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
        <Calendar :events="events" :locale="locale"
                  @click:date="showCreateEvent" @click:event="showUpdateEvent"
                  :calendar-style="{'min-height': '400px'}">
        </Calendar>
      </v-card-text>
    </v-card>

    <EventDialog ref="eventDialog" v-model="dialogEvent" :channels="channels" :updating="dialogUpdating" :loading="loading"
                 @submit="submit" @delete="showDeleteDialog" @registrations="$router.push(`/calendar/registrations?eventId=${selectedEvent.id}`)"
                 @repeat="showRepeatDialog"/>

    <EventRepeatDialog ref="eventRepeatDialog" v-model="dialogRepeat" :loading="loading" @submit="submitRepeat"/>

    <GenericDeleteDialog v-model="dialogDelete" dialog-title="Event löschen"
                         @delete="deleteEvent" :loading="loading">
      Folgendes Event wird gelöscht:
      <br>
      <b>{{ nameOfEvent }} ({{ dateOfEvent }})</b>
      <br><br>
      Möchten Sie wirklich diesen Event löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>

  </MainContainer>
</template>

<script lang="ts">
import moment from "moment"
import MainContainer from "@/components/layout/MainContainer.vue";
import {
  createEvent,
  deleteEvent,
  getAllEvents,
  getChannels,
  getEventsByChannel,
  repeatEvent,
  updateEvent,
} from "@/api";
import LocaleSelector from "@/components/LocaleSelector.vue";
import Calendar from "@/components/Calendar.vue";
import EventDialog from "@/components/dialog/EventDialog.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import EventRepeatDialog from "@/components/dialog/EventRepeatDialog.vue";
import {type ChannelDto, type CreateEventDto, type EventDto, IntervalType} from "~/models";
import {showSnackbar} from "~/utils/snackbar";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";

const channelAll = { id: -1, name: { en: 'Alle', de: 'Alle' } };

export default {
  name: 'CalendarView',
  components: {GenericDeleteDialog, GenericDialog, LoadingIndicator, EventDialog, Calendar, LocaleSelector, MainContainer, EventRepeatDialog},
  data: () => ({
    fetching: true,
    loading: false,
    channelsWithAll: [] as ChannelDto[],
    channels: [] as ChannelDto[],
    channel: channelAll as ChannelDto,
    locale: 'EN',
    events: [] as EventDto[],
    dialogEvent: false,
    dialogUpdating: false,
    dialogDelete: false,
    dialogRepeat: false,
    selectedEvent: {} as EventDto
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;

      this.channels = await getChannels({ type: 'CALENDAR' }) as ChannelDto[];
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
    showUpdateEvent: function(event: EventDto) {
      this.selectedEvent = event;
      this.$refs.eventDialog.reset(event.channel, this.locale, null);
      this.$refs.eventDialog.load(event);
      this.dialogUpdating = true;
      this.dialogEvent = true;
    },
    showDeleteDialog: function() {
      this.dialogDelete = true;
    },
    showRepeatDialog: function() {
      this.$refs.eventRepeatDialog.reset();
      this.dialogRepeat = true;
    },
    submit: async function(event: CreateEventDto) {
      if (this.dialogUpdating)
        await this.updateEvent(event);
      else
        await this.createEvent(event);
    },
    submitRepeat: async function(interval: IntervalType, endDate: string) {
      try {
        this.loading = true;
        await repeatEvent({
          eventId: this.selectedEvent.id,
          interval: interval,
          endDate: endDate
        });
        this.dialogEvent = false;
        this.dialogRepeat = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    createEvent: async function(event: CreateEventDto) {
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
    updateEvent: async function(event: CreateEventDto) {
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
        this.dialogEvent = false;
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
