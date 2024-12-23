<template>
  <GenericDialog v-model="value" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6">
          <v-text-field v-model="nameEn" label="Name (englisch)" :disabled="loading" filled hide-details></v-text-field>
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading" filled hide-details></v-text-field>
        </v-col>
      </v-row>
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
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import {showSnackbar} from "@/utils/snackbar";

export default {
  name: 'GroupDialog',
  components: {GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    dialogTitle: {
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
    load: function(nameEn: string, nameDe: string) {
      // apply post
      this.nameEn = nameEn;
      this.nameDe = nameDe;
    },
    submit: async function() {
      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      await this.$emit('submit', {
        name: {
          en: this.nameEn,
          de: this.nameDe
        }
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
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    },
  }
}
</script>
