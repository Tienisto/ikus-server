<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle" persistent>
    <template v-slot:content>
      <v-row>
        <v-col cols="8">
          <v-text-field v-model="nameEn" label="Name* (englisch)" prepend-icon="mdi-account-circle" :disabled="loading" filled />
          <v-text-field v-model="nameDe" label="Name* (deutsch)" prepend-icon=" " :disabled="loading" hide-details filled />
        </v-col>
        <v-col cols="4">
          <v-card class="secondary" style="height: 100%">
            <v-card-text class="pa-1" style="height: 100%">
              <ImagePicker ref="imagePicker" :width="125" :height="125" @select="setFile" @delete="removeFile"/>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12">
          <v-textarea v-model="place" label="Adresse" prepend-icon="mdi-map-marker" :disabled="loading" :rows="3" filled auto-grow hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="email" label="E-Mail" prepend-icon="mdi-email" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="phoneNumber" label="Telefon" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursEn" label="Öffnungszeit (EN)" prepend-icon="mdi-clock-outline" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursDe" label="Öffnungszeit (DE)" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="12" class="input--links">
          <v-textarea v-model="linksText" label="Links" placeholder="https://example.com" hint="1 Zeile = 1 Link" prepend-icon="mdi-web" :rows="2" :disabled="loading" filled hide-details/>
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
import {getFileUrl} from "@/api";
import {showSnackbar} from "@/utils";
import GenericDialog from "@/components/dialog/GenericDialog";
import ImagePicker from "@/components/input/ImagePicker";

export default {
  name: 'ContactDialog',
  components: {ImagePicker, GenericDialog},
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
    openingHoursDe: '',
    links: [],
    linksText: '',
    uploadingFile: null, // file which will be uploaded in submit
    deletingFile: false // delete file when submit
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
      this.links = [];
      this.linksText = '';
      this.uploadingFile = null;
      this.deletingFile = false;
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
      this.links = contact.links;
      this.linksText = contact.links.join('\n');

      if (contact.file) {
        this.$nextTick(() => {
          const imageUrl = getFileUrl(contact.file) + '#' + new Date().getTime();
          this.$refs.imagePicker.setImage(imageUrl);
        });
      }
    },
    setFile: function(file) {
      this.uploadingFile = file;
      this.deletingFile = false;
    },
    removeFile: function() {
      if (this.uploadingFile) {
        // just remove cache
        this.uploadingFile = null;
      } else {
        // delete file when submit
        this.deletingFile = true;
      }
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
        openingHours: { en: this.openingHoursEn, de: this.openingHoursDe },
        links: this.linksText.split('\n')
      }, {
        uploadingFile: this.uploadingFile,
        deletingFile: this.deletingFile
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

<style scoped>
  .input--links >>> textarea {
    white-space: pre;
    overflow: auto;
  }
</style>