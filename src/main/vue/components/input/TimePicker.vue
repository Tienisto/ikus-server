<template>
  <v-text-field @click="openPicker" :model-value="props.modelValue" :label="props.label" :prepend-icon="props.icon"
                hide-details readonly filled />
  <v-dialog v-model="dialog" width="290px">
    <v-time-picker v-model="internalValue" color="primary" format="24hr" full-width>
      <template #actions>
        <div class="mt-6">
          <v-btn @click="dialog = false" color="black">
            Abbrechen
          </v-btn>
          <v-btn @click="save" color="primary" prepend-icon="mdi-content-save" variant="elevated">
            Speichern
          </v-btn>
        </div>
      </template>
    </v-time-picker>
  </v-dialog>
</template>

<script setup lang="ts">
import {ref} from "vue";

const props = defineProps<{
  modelValue: string;
  label: string;
  icon?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', time: string): void
}>();

const dialog = ref(false);
const internalValue = ref<string | null>(null);

const openPicker = () => {
  internalValue.value = props.modelValue;
  dialog.value = true;
};

const save = () => {
  if (internalValue.value === null) {
    return;
  }

  emit('update:modelValue', internalValue.value);
  dialog.value = false;
};
</script>
