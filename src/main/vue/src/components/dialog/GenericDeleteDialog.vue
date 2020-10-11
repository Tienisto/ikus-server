<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">

    <template v-slot:content>
      <slot></slot>
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="$emit('delete')" color="primary" :loading="loading">
        <v-icon left>mdi-delete</v-icon>
        Löschen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";

export default {
  name: 'GenericDeleteDialog',
  components: {GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    dialogTitle: {
      type: String,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  }
}
</script>