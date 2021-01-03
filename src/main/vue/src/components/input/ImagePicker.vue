<template>
  <div style="width: 100%; height: 100%; display: flex; justify-content: center; align-items: center">

    <ImageWithButton v-if="imageUrl" :src="imageUrl" :width="width" :height="height" style="width: 100%">
      <v-btn color="white" small @click="onImageRemove" tile>
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </ImageWithButton>

    <FileUpload v-else v-slot:default="{ upload }" @select-with-content="onImageSelect">
      <v-tooltip top>
        <template v-slot:activator="{ on, attrs }">
          <v-btn @click="upload" color="primary" v-bind="attrs" v-on="on" fab>
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </template>
        <span>Bild hochladen</span>
      </v-tooltip>
    </FileUpload>

  </div>
</template>

<script>
import ImageWithButton from "@/components/ImageWithButton";
import FileUpload from "@/components/FileUpload";

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
    imageUrl: null,
  }),
  methods: {
    setImage: function(url) {
      this.imageUrl = url;
    },
    onImageSelect: function(file, content) {
      this.imageUrl = content;
      this.$emit('select', file, content);
    },
    onImageRemove: function() {
      this.imageUrl = null;
      this.$emit('delete');
    }
  },
  watch: {
    dialog: function(newVal) {
      if (newVal) {
        this.internalValue = this.value;
      }
    }
  }
}
</script>