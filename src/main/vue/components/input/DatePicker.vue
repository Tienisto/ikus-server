<template>
  <div>
    <v-text-field @click="openPicker" :model-value="valueFormatted" :label="label" :prepend-icon="icon" hide-details readonly />
    <v-dialog v-model="dialog" width="290px">
      <v-date-picker v-model="internalValue" color="primary" locale="de" :first-day-of-week="1">
        <template #actions>
          <v-btn @click="dialog = false" color="black">
            Abbrechen
          </v-btn>
          <v-btn @click="save" color="primary" prepend-icon="mdi-content-save" variant="elevated">
            Speichern
          </v-btn>
        </template>
      </v-date-picker>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import moment from "moment"

export default {
  name: 'DatePicker',
  props: {
    modelValue: {
      type: String,
      required: true
    },
    label: {
      type: String,
      required: true
    },
    icon: {
      type: String
    }
  },
  data: () => ({
    dialog: false,
    internalValue: null as Date | null
  }),
  methods: {
    openPicker: function() {
      this.internalValue = this.modelValue ? new Date(this.modelValue) : null;
      this.dialog = true;
    },
    save: function() {
      const isoFormat = moment(this.internalValue).format('YYYY-MM-DD');
      this.$emit('update:modelValue', isoFormat);
      this.dialog = false;
    }
  },
  computed: {
    valueFormatted: function() {
      return moment(this.modelValue).format('DD.MM.YYYY');
    }
  },
}
</script>
