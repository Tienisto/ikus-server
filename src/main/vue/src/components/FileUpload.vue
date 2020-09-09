<template>
  <div style="display: inline-block">
    <slot :upload="showUpload"></slot>
    <form method="POST" enctype="multipart/form-data">
      <input ref="inputUpload" name="file" type="file" @change="upload" style="display: none" multiple>
    </form>
  </div>
</template>

<script>
export default {
  name: 'FileUpload',
  methods: {
    showUpload: function() {
      this.$refs.inputUpload.click();
    },
    upload: async function(event) {
      event.preventDefault();
      await this.$emit('start', this.$refs.inputUpload.files);
    }
  }
}
</script>