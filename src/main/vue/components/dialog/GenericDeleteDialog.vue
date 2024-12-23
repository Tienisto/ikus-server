<template>
  <GenericDialog v-model="value" :title="dialogTitle">

    <template v-slot:content>
      <slot></slot>
    </template>

    <template v-slot:actions>
      <v-btn @click="value = false" color="black" :disabled="loading" variant="text">
        Abbrechen
      </v-btn>
      <v-btn @click="$emit('delete')" color="primary" :loading="loading" prepend-icon="mdi-delete" variant="elevated">
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
    modelValue: {
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
  },
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      }
    },
  },
}
</script>
