<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6">
          <v-select
              label="Gruppe"
              v-model="group"
              :items="groups" item-text="name.en" item-value="id"
              return-object
          ></v-select>
        </v-col>
        <v-col cols="6"></v-col>
        <v-col cols="6">
          <v-text-field v-model="infoEn" label="Beschreibung (englisch)" :disabled="loading"></v-text-field>
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="infoDe" label="Beschreibung (deutsch)" :disabled="loading"></v-text-field>
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="urlEn" label="Adresse (englisch)" :disabled="loading"></v-text-field>
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="urlDe" label="Adresse (deutsch)" :disabled="loading"></v-text-field>
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
  }),
  methods: {
    reset: function(group) {
      // reset input
      this.infoEn = '';
      this.infoDe = '';
      this.urlEn = '';
      this.urlDe = '';

      // apply predefined group
      this.group = group;
    },
    load: function(link) {
      // apply post
      this.infoEn = link.info.en;
      this.infoDe = link.info.de;
      this.urlEn = link.url.en;
      this.urlDe = link.url.de;
    },
    submit: async function() {
      await this.$emit('submit', {
        groupId: this.group.id,
        info: { en: this.infoEn, de: this.infoDe },
        url: { en: this.urlEn, de: this.urlDe },
      });
    }
  },
  computed: {
    dialogTitle: function() {
      return this.updating ? 'Link bearbeiten' : 'Neuer Link';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    }
  }
}
</script>