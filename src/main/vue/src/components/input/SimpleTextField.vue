<template>
  <input type="text" :placeholder="placeholder" :value="value" @input="$emit('input', $event.target.value)"
         name="asdfagragret" autocomplete="false"
         class="grey--text text--darken-1" :class="big ? ['text-h5'] : ['text-body-1']" style="border: none; width: 100%; outline: none">
</template>

<script>

export default {
  name: 'SimpleTextField',
  props: {
    value: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      default: ''
    },
    big: {
      type: Boolean,
      default: false
    }
  }
}
</script>