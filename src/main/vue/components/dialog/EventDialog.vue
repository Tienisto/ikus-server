<template>
  <GenericDialog v-model="value" :title="dialogTitle" persistent>
    <template v-slot:content>

      <div class="d-flex align-center">
        <div class="flex-grow-1">
          <v-select
              label="Kanal"
              v-model="channel"
              :items="channels" item-title="name.en" item-value="id"
              return-object hide-details
          />
        </div>
        <div class="ml-4 d-flex align-center">
          <div style="flex: 1"></div>
          <LocaleSelector v-model="locale" />
        </div>
      </div>

      <v-card color="secondary" class="mt-6">
        <v-card-text>
          <SimpleTextField v-show="locale === 'EN'" v-model="nameEn" placeholder="Name* (englisch)" :big="true"/>
          <SimpleTextField v-show="locale === 'DE'" v-model="nameDe" placeholder="Name* (deutsch)" :big="true"/>
          <SimpleTextArea  v-show="locale === 'EN'" v-model="infoEn" placeholder="Zusatzinfo (englisch)" class="mt-2" style="height: 100px"/>
          <SimpleTextArea  v-show="locale === 'DE'" v-model="infoDe" placeholder="Zusatzinfo (deutsch)"  class="mt-2" style="height: 100px"/>
        </v-card-text>
      </v-card>

      <v-row class="mt-4">
        <v-col cols="6">
          <DatePicker v-model="startDate" label="Datum*" icon="mdi-calendar"/>
        </v-col>
        <v-col cols="6">
          <TimePicker v-model="startTime" icon="mdi-clock-outline" label="Von"/>
        </v-col>
        <v-col cols="6">
          <v-checkbox v-if="sameDay" v-model="sameDay" label="gleicher Tag" class="ml-6" hide-details />
          <DatePicker v-else v-model="endDate" label="Enddatum" icon="mdi-calendar"/>
        </v-col>
        <v-col cols="6">
          <TimePicker v-model="endTime" icon="mdi-clock-outline" label="Bis"/>
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="place" label="Ort" prepend-icon="mdi-map" filled hide-details/>
        </v-col>
        <v-col cols="12">
          <ClientOnly>
            <LocationPicker v-model="coords" label="Koordinaten" icon="mdi-map-marker" />
          </ClientOnly>
        </v-col>
      </v-row>

      <div v-if="updating" class="mt-2">
        <v-btn @click="$emit('delete')" class="mt-4" color="primary" :disabled="loading" prepend-icon="mdi-delete" variant="text">
          Event Löschen
        </v-btn>
        <v-btn @click="$emit('registrations')" class="mt-4" color="primary" :disabled="loading" prepend-icon="mdi-clipboard-text" variant="text">
          Anmeldungen
        </v-btn>
        <v-btn @click="$emit('repeat')" class="mt-4" color="primary" :disabled="loading" prepend-icon="mdi-repeat" variant="text">
          Wiederholen
        </v-btn>
      </div>

    </template>

    <template v-slot:actions>
      <v-btn @click="value = false" color="black" :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading" :prepend-icon="submitIcon" variant="elevated">
        {{ submitText }}
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script lang="ts">
import moment from "moment"
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import SimpleTextField from "@/components/input/SimpleTextField.vue";
import SimpleTextArea from "@/components/input/SimpleTextArea.vue";
import TimePicker from "@/components/input/TimePicker.vue";
import DatePicker from "@/components/input/DatePicker.vue";
import LocationPicker from "@/components/input/LocationPicker.vue";
import {showSnackbar} from "~/utils/snackbar";
import type {ChannelDto, CoordsDto, EventDto} from "~/models";

export default {
  name: 'EventDialog',
  components: {LocationPicker, DatePicker, TimePicker, SimpleTextArea, SimpleTextField, LocaleSelector, GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    updating: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
    channels: {
      type: Array,
      required: true
    }
  },
  data: () => ({
    channel: {} as ChannelDto,
    locale: 'EN',
    nameEn: '',
    nameDe: '',
    infoEn: '',
    infoDe: '',
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
    place: null as string | null,
    coords: null as CoordsDto | null,
    sameDay: true // ui purpose only
  }),
  methods: {
    reset: function(channel: ChannelDto, locale: string, date: string) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.infoEn = '';
      this.infoDe = '';
      this.startDate = '';
      this.startTime = '';
      this.endDate = '';
      this.endTime = '';
      this.place = '';
      this.coords = null;
      this.sameDay = true;

      // apply prefilled values
      this.channel = channel;
      this.locale = locale;
      this.startDate = date;
      this.endDate = date;
    },
    load: function(event: EventDto) {
      // apply event
      // backend data event.startTime refer to the start timestamp (date+time)
      // vue startDate/startTime refer to date/time only
      this.nameEn = event.name.en;
      this.nameDe = event.name.de;
      if (event.info) {
        this.infoEn = event.info.en;
        this.infoDe = event.info.de;
      }

      const startTimeObj = moment(event.startTime);
      this.startDate = startTimeObj.format('YYYY-MM-DD');
      this.startTime = startTimeObj.format('HH:mm');

      if (event.endTime) {
        const endTimeObj = moment(event.endTime);
        this.endDate = endTimeObj.format('YYYY-MM-DD');
        this.endTime = endTimeObj.format('HH:mm');

        if (this.startDate !== this.endDate)
          this.sameDay = false;
      } else {
        this.endDate = this.startDate;
      }

      this.place = event.place;
      this.coords = event.coords;
    },
    submit: async function() {
      if (!this.channel.id || this.channel.id < 0) {
        showSnackbar('Bitte Kanal auswählen');
        return;
      }

      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte Event-Namen eingeben');
        return;
      }

      // prepare info
      let info = null;
      if (this.infoEn && this.infoDe) {
        info = {
          en: this.infoEn,
          de: this.infoDe
        };
      }

      // prepare time
      if (!this.startTime) {
        this.startTime = '00:00';
      }

      if (!this.sameDay && !this.endTime) {
        this.endTime = '00:00';
      }

      const startTimestamp = moment(this.startDate);
      const startTime = moment('2020-01-02T'+this.startTime);
      startTimestamp.set({
        hours: startTime.hours(),
        minutes: startTime.minutes()
      });

      let endTimestamp = null;
      if (this.endTime) {
        // apply date
        if (this.sameDay)
          endTimestamp = moment(this.startDate);
        else
          endTimestamp = moment(this.endDate);

        // apply time
        const endTime = moment('2020-01-02T'+this.endTime);
        endTimestamp.set({
          hours: endTime.hours(),
          minutes: endTime.minutes()
        });
        endTimestamp = endTimestamp.toISOString();
      }

      await this.$emit('submit', {
        channelId: this.channel.id,
        name: {
          en: this.nameEn,
          de: this.nameDe
        },
        info,
        place: this.place,
        coords: this.coords,
        startTime: startTimestamp.toISOString(),
        endTime: endTimestamp
      });
    }
  },
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value: boolean) {
        this.$emit('update:modelValue', value)
      }
    },
    dialogTitle: function() {
      return this.updating ? 'Event bearbeiten' : 'Neues Event';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    },
  }
}
</script>