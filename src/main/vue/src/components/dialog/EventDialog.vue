<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle" :persistent="true">
    <template v-slot:content>

      <v-row>
        <v-col cols="4">
          <v-select
              label="Kanal"
              v-model="channel"
              :items="channels" item-text="name.en" item-value="id"
              return-object
          ></v-select>
        </v-col>
        <v-col cols="8" style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <LocaleSelector v-model="locale" :locales="locales" />
        </v-col>
      </v-row>

      <v-card color="secondary">
        <v-card-text>
          <SimpleTextField v-show="locale === 'EN'" v-model="nameEn" placeholder="Name (englisch)" :big="true"/>
          <SimpleTextField v-show="locale === 'DE'" v-model="nameDe" placeholder="Name (deutsch)" :big="true"/>
          <SimpleTextArea  v-show="locale === 'EN'" v-model="infoEn" placeholder="Zusatzinfo (englisch)" class="mt-2" style="height: 100px"/>
          <SimpleTextArea  v-show="locale === 'DE'" v-model="infoDe" placeholder="Zusatzinfo (deutsch)"  class="mt-2" style="height: 100px"/>
        </v-card-text>
      </v-card>

      <v-row class="mt-2">
        <v-col cols="4">
          <DatePicker v-model="date" label="Datum" icon="mdi-calendar"/>
        </v-col>
        <v-col cols="4">
          <TimePicker v-model="startTime" label="Von (optional)"/>
        </v-col>
        <v-col cols="4">
          <TimePicker v-model="endTime" label="Bis (optional)"/>
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="place" label="Ort (optional)" prepend-icon="mdi-map" hide-details/>
        </v-col>
        <v-col cols="12">
          <LocationPicker v-model="coords" label="Koordinaten (optional)" icon="mdi-map-marker" />
        </v-col>
      </v-row>

      <br>

      <v-btn v-if="updating" @click="$emit('delete')" color="primary" :disabled="loading" text>
        <v-icon left>mdi-delete</v-icon>
        Event Löschen
      </v-btn>

    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading">
        <v-icon left>{{ submitIcon }}</v-icon>
        {{ submitText }}
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import moment from "moment"
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import SimpleTextField from "@/components/input/SimpleTextField";
import SimpleTextArea from "@/components/input/SimpleTextArea";
import TimePicker from "@/components/input/TimePicker";
import DatePicker from "@/components/input/DatePicker";
import {showSnackbar} from "@/utils";
import LocationPicker from "@/components/input/LocationPicker";

export default {
  name: 'EventDialog',
  components: {LocationPicker, DatePicker, TimePicker, SimpleTextArea, SimpleTextField, LocaleSelector, GenericDialog},
  props: {
    value: {
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
    },
    locales: {
      type: Array,
      required: true
    }
  },
  data: () => ({
    channel: {},
    locale: 'EN',
    nameEn: '',
    nameDe: '',
    infoEn: '',
    infoDe: '',
    date: '',
    startTime: '',
    endTime: '',
    place: '',
    coords: {}
  }),
  methods: {
    reset: function(channel, locale, date) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.infoEn = '';
      this.infoDe = '';
      this.date = '';
      this.startTime = '';
      this.endTime = '';
      this.place = '';
      this.coords = {};

      // apply prefilled values
      this.channel = channel;
      this.locale = locale;
      this.date = date;
    },
    load: function(event) {
      // apply post
      this.nameEn = event.name.en;
      this.nameDe = event.name.de;
      if (event.info) {
        this.infoEn = event.info.en;
        this.infoDe = event.info.de;
      }

      const startTimeObj = moment(event.startTime);
      this.date = startTimeObj.format('YYYY-MM-DD');
      this.startTime = startTimeObj.format('HH:mm');

      if (event.endTime) {
        this.endTime = moment(event.endTime).format('HH:mm');
      }

      this.place = event.place;
      this.coords = event.coords ? event.coords : {};
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

      const startTimestamp = moment(this.date);
      const startTime = moment('2020-01-02T'+this.startTime);
      startTimestamp.set({
        hours: startTime.hours(),
        minutes: startTime.minutes()
      });

      let endTimestamp = null;
      if (this.endTime) {
        endTimestamp = moment(this.date);
        const endTime = moment('2020-01-02T'+this.endTime);
        endTimestamp.set({
          hours: endTime.hours(),
          minutes: endTime.minutes()
        });
        endTimestamp = endTimestamp.toISOString();
      }

      // prepare coords
      const coords = this.coords && this.coords.x && this.coords.y ? this.coords : null;

      await this.$emit('submit', {
        channelId: this.channel.id,
        name: {
          en: this.nameEn,
          de: this.nameDe
        },
        info,
        place: this.place,
        coords,
        startTime: startTimestamp.toISOString(),
        endTime: endTimestamp
      });
    }
  },
  computed: {
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