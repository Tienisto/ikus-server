<template>
  <input type="text" :placeholder="placeholder" v-model="value"
         name="asdfagragret" autocomplete="false"
         :class="big ? ['text-h5'] : ['text-body-1']" style="border: none; width: 100%; outline: none">
</template>

<script lang="ts">
export default {
  name: 'SimpleTextField',
  props: {
    modelValue: {
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