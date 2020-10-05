<template>
  <div id="modal-loading" style="position: fixed; z-index: 9999; left: 0; top: 0;width: 100%; height: 100%; overflow: auto; background-color: rgb(0,0,0); background-color: rgba(0,0,0,0.4);">
    <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center">
      <v-card style="min-width: 350px">
        <v-card-text class="pa-6">
          <p class="text-h4 black--text" style="text-align: center">{{ text }}</p>
          <v-progress-linear :value="percentage * 100.0" :height="15" :indeterminate="intermediate" striped rounded></v-progress-linear>
        </v-card-text>
      </v-card>
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