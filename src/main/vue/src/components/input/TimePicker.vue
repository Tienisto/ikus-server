<template>
  <v-dialog ref="dialog" v-model="dialog" width="290px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="value" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly filled></v-text-field>
    </template>
    <v-time-picker v-if="dialog" :value="internalValue" @input="internalValue = $event" format="24hr" full-width>
      <v-spacer></v-spacer>
      <v-btn @click="dialog = false" color="black" text>
        Abbrechen
      </v-btn>
      <v-btn @click="save" color="primary">
        <v-icon left>mdi-content-save</v-icon>
        Speichern
      </v-btn>
    </v-time-picker>
  </v-dialog>
</template>

<script>

export default {
  name: 'TimePicker',
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
    internalValue: null
  }),
  methods: {
    save: function() {
      this.$emit('input', this.internalValue);
      this.dialog = false;
    }
  },
  watch: {
    dialog: function(newVal) {
      if (newVal) {
        this.internalValue = this.value;
      }
    }
  }
}
</script>