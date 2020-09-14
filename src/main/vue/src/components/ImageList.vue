<template>
  <v-row>
    <v-col cols="4" v-for="f in files" :key="f.id">
      <div style="position: relative; height: 150px">
        <div style="position: absolute; top: 0; right: 0; z-index: 2">
          <v-btn color="white" small @click="$emit('delete', f)" tile>
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
        <div style="position: absolute; top: 0; right: 0">
          <v-img :src="fileUrl(f)" height="150" width="180" />
        </div>
      </div>
    </v-col>
    <v-col cols="4">
      <div style="height: 100%; display: flex; align-items: center;" :style="files.length !== 0 ? { 'justify-content': 'center' } : {}">
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