<template>
  <div style="display: flex; align-items: center; justify-content: space-between">
    <span class="mr-4">Sprache: </span>
    <v-btn-toggle v-model="value" color="primary" variant="outlined" mandatory density="compact">
      <v-btn :value="l" v-for="l in locales" :key="l">
        {{ l }}
      </v-btn>
    </v-btn-toggle>
  </div>
</template>

<script lang="ts">
export default {
  name: 'LocaleSelector',
  props: {
    modelValue: {
      type: String,
      default: 'EN'
    }
  },
  data: () => ({
    locales: ['EN', 'DE']
  }),
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      }
    },
  },
}
</script>
