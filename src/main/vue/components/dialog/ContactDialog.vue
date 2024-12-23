<template>
  <GenericDialog v-model="value" :title="dialogTitle" persistent>
    <template v-slot:content>
      <v-row>
        <v-col cols="8">
          <v-text-field v-model="nameEn" label="Name* (englisch)" prepend-icon="mdi-account-circle" :disabled="loading" filled />
          <v-text-field v-model="nameDe" label="Name* (deutsch)" prepend-icon=" " :disabled="loading" hide-details filled />
        </v-col>
        <v-col cols="4">
          <v-card class="bg-secondary" style="height: 100%">
            <v-card-text class="pa-1" style="height: 100%">
              <ImagePicker ref="image-picker" :width="125" :height="125" @select="setFile" @delete="removeFile"/>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12">
          <v-textarea v-model="place" label="Adresse" prepend-icon="mdi-map-marker" :disabled="loading" :rows="3" filled auto-grow hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="email" label="E-Mail" prepend-icon="mdi-email" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="phoneNumber" label="Telefon" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursEn" label="Öffnungszeit (EN)" prepend-icon="mdi-clock-outline" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="openingHoursDe" label="Öffnungszeit (DE)" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="12" class="input--links">
          <v-textarea v-model="linksText" label="Links" placeholder="https://example.com" hint="1 Zeile = 1 Link" prepend-icon="mdi-web" :rows="2" :disabled="loading" filled hide-details/>
        </v-col>
      </v-row>
    </template>

    <template v-slot:actions>
      <v-btn @click="value = false" color="black" :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading" :prepend-icon="submitIcon" variant="elevated">
        {{ submitText }}
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script lang="ts" setup>
import {getFileUrl} from "@/api";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import ImagePicker from "@/components/input/ImagePicker.vue";
import {showSnackbar} from "@/utils/snackbar";
import {computed, nextTick, ref, useTemplateRef} from "vue";
import type {ContactDto} from "@/models";

const imagePicker = useTemplateRef<InstanceType<typeof ImagePicker>>('image-picker');

export interface ContactPostAction {
  uploadingFile: File | null;
  deletingFile: boolean;
}

const props = defineProps<{
  modelValue: boolean;
  updating: boolean;
  loading: boolean;
}>();

const emit = defineEmits(['update:modelValue', 'submit']);

defineExpose({ reset, load });

const nameEn = ref('');
const nameDe = ref('');
const place = ref('' as undefined | string);
const email = ref('' as undefined | string);
const phoneNumber = ref('' as undefined | string);
const openingHoursEn = ref('' as undefined | string);
const openingHoursDe = ref('' as undefined | string);
const links = ref([] as string[]);
const linksText = ref('');
const uploadingFile = ref<File | null>(null); // file which will be uploaded in submit
const deletingFile = ref(false); // delete file when submit

function reset() {
  // reset input
  nameEn.value = '';
  nameDe.value = '';
  place.value = '';
  email.value = '';
  phoneNumber.value = '';
  openingHoursEn.value = '';
  openingHoursDe.value = '';
  links.value = [];
  linksText.value = '';
  uploadingFile.value = null;
  deletingFile.value = false;
}

function load(contact: ContactDto) {
  // apply
  nameEn.value = contact.name.en;
  nameDe.value = contact.name.de;
  place.value = contact.place;
  email.value = contact.email;
  phoneNumber.value = contact.phoneNumber;
  openingHoursEn.value = contact.openingHours ? contact.openingHours.en : '';
  openingHoursDe.value = contact.openingHours ? contact.openingHours.de : '';
  links.value = contact.links;
  linksText.value = contact.links.join('\n');

  if (contact.file) {
    nextTick(() => {
      nextTick(() => {
        const imageUrl = getFileUrl(contact.file!) + '#' + new Date().getTime();
        imagePicker.value!.setImage(imageUrl);
      });
    });
  }
}

function setFile(file: File) {
  uploadingFile.value = file;
  deletingFile.value = false;
}

function removeFile() {
  if (uploadingFile.value) {
    // just remove cache
    uploadingFile.value = null;
  } else {
    // delete file when submit
    deletingFile.value = true;
  }
}

function submit() {

  if (!nameEn.value || !nameDe.value) {
    showSnackbar('Bitte Namen eingeben');
    return;
  }

  if ((!openingHoursEn.value && openingHoursDe.value) || (openingHoursEn.value && !openingHoursDe.value)) {
    showSnackbar('Öffnungszeiten beidsprachig angeben');
    return;
  }

  emit('submit', {
    name: { en: nameEn.value, de: nameDe.value },
    place: place.value,
    email: email.value,
    phoneNumber: phoneNumber.value,
    openingHours: { en: openingHoursEn.value, de: openingHoursDe.value },
    links: linksText.value.split('\n')
  }, {
    uploadingFile: uploadingFile.value,
    deletingFile: deletingFile.value
  } as ContactPostAction);
}

const value = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

const dialogTitle = computed(() => props.updating ? 'Kontakt bearbeiten' : 'Neuer Kontakt');
const submitText = computed(() => props.updating ? 'Speichern' : 'Erstellen');
const submitIcon = computed(() => props.updating ? 'mdi-content-save' : 'mdi-plus');
</script>

<style scoped>
  .input--links >>> textarea {
    white-space: pre;
    overflow: auto;
  }
</style>
