<template>
  <v-dialog ref="dialog" v-model="dialog" width="290px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="valueFormatted" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly filled></v-text-field>
    </template>
    <v-date-picker :value="value" @input="$emit('input', $event)" locale="de" :first-day-of-week="1" scrollable>
      <v-spacer></v-spacer>
      <v-btn text color="primary" @click="dialog = false">Schließen</v-btn>
    </v-date-picker>
  </v-dialog>
</template>

<script>
import moment from "moment"

export default {
  name: 'DatePicker',
  props: {
    value: {
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
    dialog: false
  }),
  computed: {
    valueFormatted: function() {
      return moment(this.value).format('DD.MM.YYYY');
    }
  }
}
</script>