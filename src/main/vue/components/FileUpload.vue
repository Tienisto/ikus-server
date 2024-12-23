<template>
  <div style="display: inline-block">
    <slot :upload="showUpload"></slot>
    <form method="POST" enctype="multipart/form-data">
      <input ref="inputUpload" name="file" type="file" @change="upload" style="display: none" :multiple="multiple">
    </form>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";

const props = defineProps({
  multiple: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(['select', 'select-with-content']);
const inputUpload = ref<HTMLInputElement | null>(null)

function showUpload() {
  inputUpload.value!.click();
}

function upload(event: Event) {
  event.preventDefault();

  const file = inputUpload!.value!.files![0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const content = e.target!.result;
    emit('select-with-content', file, content);
  }
  reader.readAsDataURL(file);

  emit('select', props.multiple ? inputUpload.value!.files : inputUpload!.value!.files![0]);
}
</script>
