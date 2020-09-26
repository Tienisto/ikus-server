<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6">
          <v-text-field v-model="nameEn" label="Name (englisch)" prepend-icon="mdi-account-circle" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="12">
          <v-textarea v-model="place" label="Adresse" prepend-icon="mdi-map-marker" :disabled="loading" auto-grow solo hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="email" label="E-Mail" prepend-icon="mdi-email" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="phoneNumber" label="Telefon" prepend-icon="mdi-phone" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursEn" label="Öffnungszeiten (en)" prepend-icon="mdi-clock-outline" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursDe" label="Öffnungszeiten (de)" :disabled="loading" hide-details />
        </v-col>
      </v-row>
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
import {showSnackbar} from "@/utils";
import GenericDialog from "@/components/dialog/GenericDialog";

export default {
  name: 'ContactDialog',
  components: {GenericDialog},
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
    }
  },
  data: () => ({
    nameEn: '',
    nameDe: '',
    place: '',
    email: '',
    phoneNumber: '',
    openingHoursEn: '',
    openingHoursDe: ''
  }),
  methods: {
    reset: function() {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.place = '';
      this.email = '';
      this.phoneNumber = '';
      this.openingHoursEn = '';
      this.openingHoursDe = '';
    },
    load: function(contact) {
      // apply
      this.nameEn = contact.name.en;
      this.nameDe = contact.name.de;
      this.place = contact.place;
      this.email = contact.email;
      this.phoneNumber = contact.phoneNumber;
      this.openingHoursEn = contact.openingHours ? contact.openingHours.en : '';
      this.openingHoursDe = contact.openingHours ? contact.openingHours.de : '';
    },
    submit: async function() {

      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte Namen eingeben');
        return;
      }

      if ((!this.openingHoursEn && this.openingHoursDe) || (this.openingHoursEn && !this.openingHoursDe)) {
        showSnackbar('Öffnungszeiten beidsprachig angeben');
        return;
      }

      await this.$emit('submit', {
        name: { en: this.nameEn, de: this.nameDe },
        place: this.place,
        email: this.email,
        phoneNumber: this.phoneNumber,
        openingHours: { en: this.openingHoursEn, de: this.openingHoursDe }
      });
    }
  },
  computed: {
    dialogTitle: function() {
      return this.updating ? 'Kontakt bearbeiten' : 'Neuer Kontakt';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    }
  },
}
</script>