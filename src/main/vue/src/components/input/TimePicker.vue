<template>
  <v-dialog ref="dialog" v-model="dialog" width="290px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="value" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly filled></v-text-field>
    </template>
    <v-time-picker v-if="dialog" :value="value" @input="$emit('input', $event)" format="24hr" full-width>
      <v-spacer></v-spacer>
      <v-btn text color="primary" @click="dialog = false">Schließen</v-btn>
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
    dialog: false
  }),
}
</script>