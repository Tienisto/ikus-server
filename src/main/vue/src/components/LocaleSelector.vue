<template>
  <div style="display: flex; align-items: center; justify-content: space-between">
    <span class="mr-4">Sprache: </span>
    <v-btn-toggle :value="value" @change="$emit('input', $event)" color="primary" mandatory dense>
      <v-btn :value="l" v-for="l in locales" :key="l" text>
        {{ l }}
      </v-btn>
    </v-btn-toggle>
  </div>
</template>

<script>
export default {
  name: 'LocaleSelector',
  props: {
    value: {
      type: String,
      default: 'EN'
    }
  },
  data: () => ({
    locales: ['EN', 'DE']
  })
}
</script>