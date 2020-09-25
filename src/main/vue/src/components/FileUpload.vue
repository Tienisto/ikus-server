<template>
  <div style="display: inline-block">
    <slot :upload="showUpload"></slot>
    <form method="POST" enctype="multipart/form-data">
      <input ref="inputUpload" name="file" type="file" @change="upload" style="display: none" :multiple="multiple">
    </form>
  </div>
</template>

<script>
export default {
  name: 'FileUpload',
  props: {
    multiple: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    showUpload: function() {
      this.$refs.inputUpload.click();
    },
    upload: async function(event) {
      event.preventDefault();
      await this.$emit('select', this.multiple ? this.$refs.inputUpload.files : this.$refs.inputUpload.files[0]);
    }
  }
}
</script>