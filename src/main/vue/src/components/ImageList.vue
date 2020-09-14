<template>
  <v-row>
    <v-col cols="4" v-for="f in files" :key="f.id">
      <div style="position: relative; top: 27px; text-align: right; z-index: 10">
        <v-btn text color="white" small @click="$emit('delete', f)">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
      <v-img :src="fileUrl(f)" height="150"></v-img>
    </v-col>
    <v-col cols="4">
      <div style="height: 100%; display: flex; align-items: center; justify-content: center;">
        <FileUpload v-slot:default="{ upload }" @start="$emit('upload', $event)">
          <v-btn @click="upload" class="primary" fab>
            <v-icon>mdi-upload</v-icon>
          </v-btn>
        </FileUpload>
      </div>
    </v-col>
  </v-row>
</template>

<script>
import FileUpload from "@/components/FileUpload";
import {getFileUrl} from "@/api";

export default {
  name: 'ImageList',
  components: {FileUpload},
  props: {
    files: {
      type: Array,
      required: true
    }
  },
  computed: {
    fileUrl: function() {
      return (file) => getFileUrl({ name: file.fileName });
    }
  },
}
</script>