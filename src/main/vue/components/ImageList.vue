<template>
  <v-row>
    <v-col cols="4" v-for="f in files" :key="f.id">
      <ImageWithButton :src="fileUrl(f)" :width="180" :height="150">
        <v-btn color="white" small @click="$emit('delete', f)" tile>
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </ImageWithButton>
    </v-col>
    <v-col cols="4">
      <div style="height: 100%; display: flex; align-items: center;" :style="files.length !== 0 ? { 'justify-content': 'center' } : {}">
        <FileUpload v-slot:default="{ upload }" @select="$emit('upload', $event)" multiple>
          <v-btn @click="upload" class="bg-primary" icon="mdi-upload" />
        </FileUpload>
      </div>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import FileUpload from "@/components/FileUpload.vue";
import ImageWithButton from "@/components/ImageWithButton.vue";
import {getFileUrl} from "@/api";
import type {PropType} from "vue";
import type {PostFileDto} from "~/models";

export default {
  name: 'ImageList',
  components: {ImageWithButton, FileUpload},
  props: {
    files: {
      type: Array as PropType<PostFileDto[]>,
      required: true
    }
  },
  computed: {
    fileUrl: function() {
      return (file: PostFileDto) => getFileUrl(file.fileName);
    }
  },
}
</script>
