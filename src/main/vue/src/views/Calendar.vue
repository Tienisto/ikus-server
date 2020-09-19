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
          :items="channels" item-text="name.en" item-value="id"
          return-object
      />

      <LocaleSelector v-model="locale" :locales="locales"/>

      <br>

      <v-btn color="primary" block :disabled="loading || channels.length === 0">
        <v-icon left>mdi-plus</v-icon>
        Neues Event
      </v-btn>
    </template>

    <v-card>
      <v-card-text>
        <Calendar ref="calendar" :events="events" :locale="locale" @click:day="showCreateEvent" @click:event="showUpdateEvent">
          <template v-slot:header="{ currMonth }">
            <div style="display: flex; align-items: center; justify-content: space-between">
              <v-btn @click="$refs.calendar.prev()" color="black" text>
                <v-icon dark>mdi-chevron-left</v-icon>
              </v-btn>
              <span class="text-h6">{{ currMonth }}</span>
              <v-btn @click="$refs.calendar.next()" color="black" text>
                <v-icon dark>mdi-chevron-right</v-icon>
              </v-btn>
            </div>
            <br>
          </template>
        </Calendar>
      </v-card-text>
    </v-card>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";
import {getAllEvents, getChannels, getEventsByChannel} from "@/api";
import LocaleSelector from "@/components/LocaleSelector";
import Calendar from "@/components/Calendar";

const channelAll = { id: -1, name: { en: 'Alle', de: 'Alle' } };

export default {
  name: 'CalendarView',
  components: {Calendar, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: [],
    channel: {},
    locales: ['EN', 'DE'],
    locale: 'EN',
    events: [],
    today: new Date()
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;

      const channels = await getChannels({ type: 'CALENDAR' });
      this.channels = [ channelAll, ...channels ];
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
    showCreateEvent: function(date) {
      console.log(date);
    },
    showUpdateEvent: function(event) {
      console.log(event);
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>