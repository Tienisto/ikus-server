<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6">
          <v-text-field v-model="nameEn" label="Name (englisch)" :disabled="loading"></v-text-field>
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading"></v-text-field>
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
  name: 'ChannelDialog',
  components: {GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    channelType: {
      type: String,
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
    nameDe: ''
  }),
  methods: {
    reset: function() {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
    },
    load: function(nameEn, nameDe) {
      // apply post
      this.nameEn = nameEn;
      this.nameDe = nameDe;
    },
    submit: async function() {
      await this.$emit('submit', {
        name: {
          en: this.nameEn,
          de: this.nameDe
        }
      });
    }
  },
  computed: {
    dialogTitle: function() {
      if (this.channelType === 'FAQ')
        return this.updating ? 'Gruppe umbenennen' : 'Neue Gruppe';
      else
        return this.updating ? 'Kanal umbenennen' : 'Neuer Kanal';
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