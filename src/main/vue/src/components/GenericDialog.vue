<template>
  <v-dialog :value="value" @input="$emit('input', $event)" width="500">
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
  props: ['value', 'title']
}
</script>