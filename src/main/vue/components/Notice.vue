<template>
  <div style="display: flex; align-items: center">
    <v-icon class="text-grey-lighten-1" :size="info ? 50 : 30">{{ icon }}</v-icon>
    <div class="ml-2 text-grey-lighten-1">
      <div class="text-h5">{{ title }}</div>
      <div class="text-subtitle-1">{{ info }}</div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'Notice',
  props: {
    icon: {
      type: String,
      default: 'mdi-information-outline'
    },
    title: String,
    info: String
  }
}
</script>
