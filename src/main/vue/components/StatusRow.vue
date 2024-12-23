<template>
  <div style="display: flex; align-items: center; justify-content: space-between">
    <span>{{ keyText }}</span>
    <span v-if="valueText">{{ valueText }}</span>
    <span v-if="valueOk" class="text-green">{{ okText }}</span>
    <span v-if="valueError" class="text-red">{{ errorText }}</span>
  </div>
</template>

<script lang="ts">
export default {
  props: {
    keyText: {
      type: String,
      required: true
    },
    valueText: {
      type: String
    },
    valueOk: {
      type: Boolean
    },
    okText: {
      type: String,
      default: 'OK'
    },
    valueError: {
      type: Boolean
    },
    errorText: {
      type: String,
      default: 'ERROR'
    },
  }
}
</script>
