<template>
  <v-card>
    <v-card-text class="pt-6 pb-6" style="text-align: center">
      <span class="text-h3">{{ number }}</span>
      <br>
      <span class="text-h5">{{ label }}</span>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  name: 'NumberCard',
  props: {
    label: {
      type: String,
      required: true
    },
    number: {
      type: Number,
      required: true
    }
  }
}
</script>