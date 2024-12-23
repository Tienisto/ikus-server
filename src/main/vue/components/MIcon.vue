<template>
  <i @click="$emit('click')" class="material-icons" :class="outlined ? ['material-icons-outlined'] : []"
     style="user-select: none">
    {{ icon }}
  </i>
</template>

<script>
export default {
  name: 'MIcon',
  props: {
    icon: {
      type: String,
      required: true
    },
    outlined: {
      type: Boolean,
      default: false
    }
  }
}
</script>
