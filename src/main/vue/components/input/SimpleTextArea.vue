<template>
  <textarea type="text" :placeholder="placeholder" v-model="value"
            name="asdfagragret" autocomplete="false"
            class="text-grey-darken-1 text-body-1" style="border: none; width: 100%; outline: none; resize: none"></textarea>
</template>

<script lang="ts">
export default {
  name: 'SimpleTextArea',
  props: {
    modelValue: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      default: ''
    }
  },
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value: string) {
        this.$emit('update:modelValue', value)
      }
    },
  }
}
</script>
