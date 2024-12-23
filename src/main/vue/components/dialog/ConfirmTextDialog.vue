<template>
  <GenericDialog v-model="value" :title="title">
    <template v-slot:content>
      <slot></slot>
      <br><br>
      <v-text-field v-model="textInput" label="Eingabe" :disabled="loading"></v-text-field>
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
import GenericDialog from "@/components/dialog/GenericDialog";
import {showSnackbar} from "@/utils/snackbar";

export default {
  name: 'ConfirmTextDialog',
  components: {GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    confirmText: {
      type: String,
      required: true
    },
    submitText: {
      type: String,
      default: 'Löschen'
    },
    submitIcon: {
      type: String,
      default: 'mdi-delete'
    },
  },
  data: () => ({
    textInput: ''
  }),
  methods: {
    reset: function() {
      this.textInput = '';
    },
    submit: function() {
      if (this.textInput !== this.confirmText) {
        showSnackbar('Eingabe stimmt nicht überein');
        return;
      }
      this.$emit('submit');
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
  }
}
</script>
