<template>
  <v-tooltip :text="tooltip" location="top">
    <template v-slot:activator="{ props }">
      <v-btn @click="$emit('click')" v-bind="props"
             tile variant="flat" :icon="icon"
             :class="active ? ['bg-primary', 'text-white'] : ['bg-secondary', 'text-black']" />
    </template>
  </v-tooltip>
</template>

<script lang="ts">
export default {
  name: 'ToolbarButton',
  props: {
    active: {
      type: Boolean,
      default: false
    },
    icon: {
      type: String,
      required: true
    },
    tooltip: {
      type: String,
      default: ''
    }
  }
}
</script>
<script setup lang="ts">
</script>