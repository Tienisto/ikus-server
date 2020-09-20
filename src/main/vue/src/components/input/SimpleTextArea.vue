<template>
  <textarea type="text" :placeholder="placeholder" :value="value" @input="$emit('input', $event.target.value)"
            name="asdfagragret" autocomplete="false"
            class="grey--text text--darken-1 text-body-1" style="border: none; width: 100%; outline: none; resize: none"></textarea>
</template>

<script>

export default {
  name: 'SimpleTextArea',
  props: {
    value: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      default: ''
    }
  }
}
</script>