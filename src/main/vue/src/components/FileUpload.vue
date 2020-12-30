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
    upload: function(event) {
      event.preventDefault();

      if (Object.prototype.hasOwnProperty.call(this.$listeners, 'select-with-content')) {

        // only emit 'select-with-content' event if parent is listening
        // adds the content as 2nd argument (in base64)

        const file = this.$refs.inputUpload.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
          const content = e.target.result;
          this.$emit('select-with-content', file, content);
        }
        reader.readAsDataURL(file);
      }

      this.$emit('select', this.multiple ? this.$refs.inputUpload.files : this.$refs.inputUpload.files[0]);
    }
  }
}
</script>