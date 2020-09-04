<template>
  <v-dialog :value="value" @input="$emit('input', $event)" :width="width">
    <v-card>
      <v-card-title class="headline">
        {{ title }}
      </v-card-title>

      <v-card-text>
        <slot name="content"></slot>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <slot name="actions"></slot>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: 'GenericDialog',
  props: {
    value: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    width: {
      type: Number,
      default: 500
    }
  }
}
</script>