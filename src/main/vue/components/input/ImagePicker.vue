<template>
  <div style="width: 100%; height: 100%; display: flex; justify-content: center; align-items: center">

    <ImageWithButton v-if="imageUrl" :src="imageUrl" :width="width" :height="height" style="width: 100%">
      <v-btn color="white" small @click="onImageRemove">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </ImageWithButton>

    <FileUpload v-else v-slot:default="{ upload }" @select-with-content="onImageSelect">
      <v-tooltip text="Bild hochladen" location="top">
        <template v-slot:activator="{ props }">
          <v-btn @click="upload" color="primary" v-bind="props" fab>
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </template>
      </v-tooltip>
    </FileUpload>

  </div>
</template>

<script lang="ts">
import ImageWithButton from "@/components/ImageWithButton.vue";
import FileUpload from "@/components/FileUpload.vue";

export default {
  name: 'ImagePicker',
  components: {FileUpload, ImageWithButton},
  props: {
    width: {
      type: Number,
      required: true
    },
    height: {
      type: Number,
      required: true
    }
  },
  data: () => ({
    imageUrl: null as string | null,
  }),
  methods: {
    setImage: function(url: string) {
      this.imageUrl = url;
    },
    onImageSelect: function(file: File, content: string) {
      this.imageUrl = content;
      this.$emit('select', file, content);
    },
    onImageRemove: function() {
      this.imageUrl = null;
      this.$emit('delete');
    }
  }
}
</script>
