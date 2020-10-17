<template>
  <v-dialog ref="dialog" v-model="dialog" width="290px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="valueFormatted" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly filled></v-text-field>
    </template>
    <v-date-picker :value="currValue" @input="currValue = $event" locale="de" :first-day-of-week="1" scrollable>
      <v-spacer></v-spacer>
      <v-btn @click="dialog = false" color="black" text>
        Abbrechen
      </v-btn>
      <v-btn @click="save" color="primary">
        <v-icon left>mdi-content-save</v-icon>
        Speichern
      </v-btn>
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
    dialog: false,
    currValue: null
  }),
  methods: {
    save: function() {
      this.$emit('input', this.currValue);
      this.dialog = false;
    }
  },
  computed: {
    valueFormatted: function() {
      return moment(this.value).format('DD.MM.YYYY');
    }
  },
  watch: {
    dialog: function(newVal) {
      if (newVal) {
        this.currValue = this.value;
      }
    }
  }
}
</script>