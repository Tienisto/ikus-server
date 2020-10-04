<template>
  <div id="modal-loading" style="position: fixed; z-index: 9999; left: 0; top: 0;width: 100%; height: 100%; overflow: auto; background-color: rgb(0,0,0); background-color: rgba(0,0,0,0.4);">
    <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center">
      <div style="min-width: 250px; text-align: center; padding: 25px; border-radius: 10px; background-color: white">
        <p class="text-h6">{{ text }}</p>
        <v-progress-linear :value="percentage * 100.0" :height="10" :indeterminate="intermediate" striped></v-progress-linear>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'LoadingOverlay',
  props: {
    text: {
      type: String,
      default: 'Verarbeitung...'
    },
    percentage: {
      type: Number,
      default: 0
    },
    intermediate: {
      type: Boolean,
      default: false
    }
  }
}
</script>