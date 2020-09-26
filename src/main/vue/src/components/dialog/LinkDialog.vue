<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6">
          <v-select
              label="Gruppe"
              v-model="group"
              :items="groups" item-text="name.en" item-value="id"
              hide-details
              return-object
          ></v-select>
        </v-col>
        <v-col cols="6"></v-col>
        <v-col cols="6">
          <v-text-field v-model="infoEn" label="Beschreibung (englisch)" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="infoDe" label="Beschreibung (deutsch)" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="urlEn" :label="urlEnLabel" placeholder="https://example.com" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="12">
          <v-checkbox v-model="sameUrl" label="deutsche Seite = englische Seite" :disabled="loading" hide-details />
        </v-col>
        <v-col cols="12">
          <v-text-field v-show="!sameUrl" v-model="urlDe" label="Adresse (deutsch)" placeholder="https://example.com" :disabled="loading || sameUrl" />
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
import GenericDialog from "@/components/dialog/GenericDialog";
import {showSnackbar} from "@/utils";

export default {
  name: 'LinkDialog',
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
    },
    groups: {
      type: Array,
      required: true
    }
  },
  data: () => ({
    group: {},
    infoEn: '',
    infoDe: '',
    urlEn: '',
    urlDe: '',
    sameUrl: true
  }),
  methods: {
    reset: function(group) {
      // reset input
      this.infoEn = '';
      this.infoDe = '';
      this.urlEn = '';
      this.urlDe = '';
      this.sameUrl = true;

      // apply predefined group
      this.group = group;
    },
    load: function(link) {
      // apply post
      this.infoEn = link.info.en;
      this.infoDe = link.info.de;
      this.urlEn = link.url.en;
      this.urlDe = link.url.de;
      this.sameUrl = link.url.en === link.url.de;
    },
    submit: async function() {

      if (!this.infoEn || !this.infoDe) {
        showSnackbar('Bitte Beschreibungen eingeben');
        return;
      }

      if ((this.sameUrl && !this.urlEn) || (!this.sameUrl && (!this.urlEn || !this.urlDe))) {
        showSnackbar('Bitte Internet-Adresse eingeben');
        return;
      }

      if (!this.group.id) {
        showSnackbar('Bitte Gruppe festlegen');
        return;
      }

      const url = this.sameUrl ? { en: this.urlEn, de: this.urlEn } : { en: this.urlEn, de: this.urlDe };

      if (!url.en.startsWith('http://') && !url.en.startsWith('https://')) {
        showSnackbar('Seite muss mit http:// oder https:// beginnen');
        return;
      }

      if (!url.de.startsWith('http://') && !url.de.startsWith('https://')) {
        showSnackbar('Seite muss mit http:// oder https:// beginnen');
        return;
      }

      await this.$emit('submit', {
        groupId: this.group.id,
        info: { en: this.infoEn, de: this.infoDe },
        url,
      });
    }
  },
  computed: {
    dialogTitle: function() {
      return this.updating ? 'Link bearbeiten' : 'Neuer Link';
    },
    urlEnLabel: function() {
      return this.sameUrl ? 'Adresse' : 'Adresse (englisch)';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    }
  },
  watch: {
    urlEn: function(newVal) {
      if (this.sameUrl) {
        this.urlDe = newVal;
      }
    },
    sameUrl: function(newVal) {
      if (newVal) {
        this.urlDe = this.urlEn;
      }
    }
  }
}
</script>