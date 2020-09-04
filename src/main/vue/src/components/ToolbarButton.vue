<template>
  <v-tooltip top>
    <template v-slot:activator="{ on, attrs }">
      <v-btn @click="$emit('click')" v-bind="attrs" v-on="on"
             depressed tile small :color="active ? 'primary' : null">
        <slot></slot>
      </v-btn>
    </template>
    <span>{{ tooltip }}</span>
  </v-tooltip>
</template>

<script>
export default {
  name: 'ToolbarButton',
  props: {
    active: {
      type: Boolean,
      default: false
    },
    tooltip: {
      type: String,
      default: ''
    }
  }
}
</script>