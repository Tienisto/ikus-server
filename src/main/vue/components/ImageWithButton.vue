<template>
  <div>
    <div style="position: relative" :style="height ? { 'height': height + 'px' } : {}">
      <div style="position: absolute; top: 0; right: 0; z-index: 2">
        <slot></slot>
      </div>
      <div style="position: absolute; top: 0; right: 0">
        <v-img :src="src" :height="height" :width="width" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'ImageWithButton',
  props: {
    src: {
      type: String,
      required: false
    },
    height: {
      type: Number,
      required: false
    },
    width: {
      type: Number,
      required: false
    }
  }
}
</script>
