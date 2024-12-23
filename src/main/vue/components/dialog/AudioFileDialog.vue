<template>
  <GenericDialog v-model="value" :title="dialogTitle" persistent>
    <template v-slot:content>

      <v-row>
        <v-col cols="4">
          {{ audioGroup && audioGroup.name ? localized(audioGroup.name) : null }}
        </v-col>
        <v-col cols="8" style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <LocaleSelector v-model="locale" />
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="6" style="display: flex; align-items: center; justify-content: end">
          Bild:
        </v-col>
        <v-col cols="6">
          <v-card class="bg-secondary" style="height: 100%">
            <v-card-text class="pa-1" style="height: 100%; min-height: 210px">
              <ImagePicker ref="imagePicker" :width="200" :height="200" @select="setImage" @delete="deleteImage"/>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <div style="display: flex; align-items: center" class="mt-2">
        <div style="flex: 1" class="pr-2">
          <audio ref="audio" v-if="hasAudio" controls="controls" style="width: 100%; outline: none">
            <source :src="locale === 'EN' ? audioDataEn : audioDataDe" />
          </audio>
          <span v-else>Bitte rechts eine Audio-Datei hochladen.</span>
        </div>
        <FileUpload v-slot:default="{ upload }" @select-with-content="setAudioFile">
          <v-tooltip text="Datei hochladen" location="top">
            <template v-slot:activator="{ props }">
              <v-btn @click="upload" color="primary" v-bind="props" icon="mdi-upload" />
            </template>
          </v-tooltip>
        </FileUpload>
      </div>

      <v-text-field v-model="nameEn" label="Name" v-if="locale === 'EN'" class="mt-4" :disabled="loading" filled hide-details />
      <v-text-field v-model="nameDe" label="Name" v-else class="mt-4" :disabled="loading" filled hide-details />

      <v-textarea v-model="textEn" label="Text (optional)" v-if="locale === 'EN'" class="mt-4" :disabled="loading" :rows="6" filled hide-details />
      <v-textarea v-model="textDe" label="Text (optional)" v-else class="mt-4" :disabled="loading" :rows="6" filled hide-details />

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

<script lang="ts">
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import {getFileUrl} from "@/api";
import FileUpload from "@/components/FileUpload";
import ImagePicker from "@/components/input/ImagePicker";
import {localizedString} from "@/utils/localization";
import {showSnackbar} from "@/utils/snackbar";

export default {
  name: 'AudioFileDialog',
  components: {ImagePicker, FileUpload, LocaleSelector, GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    updating: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  },
  data: () => ({
    audioGroup: {},
    locale: 'EN',
    nameEn: '',
    nameDe: '',
    textEn: '',
    textDe: '',
    imageEn: '',
    imageDe: '',
    uploadingAudioFileEn: null,
    uploadingAudioFileDe: null,
    uploadingImageFileEn: null,
    uploadingImageFileDe: null,
    deletingImageFileEn: false,
    deletingImageFileDe: false,
    audioDataEn: null, // display purpose only, can be plain url or encoded
    audioDataDe: null // display purpose only, can be plain url or encoded
  }),
  methods: {
    reset: function(audioGroup, locale) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.textEn = '';
      this.textDe = '';
      this.imageEn = null;
      this.imageDe = null;
      this.uploadingAudioFileEn = null;
      this.uploadingAudioFileDe = null;
      this.uploadingImageFileEn = null;
      this.uploadingImageFileDe = null;
      this.deletingImageFileEn = null;
      this.deletingImageFileDe = null;
      this.audioDataEn = null;
      this.audioDataDe = null;

      // apply preset
      this.audioGroup = audioGroup;
      this.locale = locale;

      this.$nextTick(() => {
        this.$nextTick(() => {
          // somehow this need 2 ticks
          this.updateImageBox();
        });
      });
    },
    load: function(audio) {
      // apply
      this.nameEn = audio.name.en;
      this.nameDe = audio.name.de;
      this.textEn = audio.text ? audio.text.en : '';
      this.textDe = audio.text ? audio.text.de : '';
      this.audioDataEn = getFileUrl(audio.file.en) + '#' + new Date().getTime();
      this.audioDataDe = getFileUrl(audio.file.de) + '#' + new Date().getTime();
      this.updateAudioBox();

      if (audio.image) {
        this.imageEn = getFileUrl(audio.image.en) + '#' + new Date().getTime();
        this.imageDe = getFileUrl(audio.image.de) + '#' + new Date().getTime();
        this.updateImageBox();
      }
    },
    updateAudioBox: function() {
      this.$nextTick(() => {
        if (this.hasAudio)
          this.$refs.audio.load();
      });
    },
    setAudioFile: function(file, content) {
      if (this.locale === 'EN') {
        this.audioDataEn = content;
        this.uploadingAudioFileEn = file;
      } else {
        this.audioDataDe = content;
        this.uploadingAudioFileDe = file;
      }
      this.updateAudioBox();
    },
    updateImageBox: function() {
      this.$nextTick(() => {
        if (this.locale === 'EN') {
          this.$refs.imagePicker.setImage(this.imageEn);
        } else {
          this.$refs.imagePicker.setImage(this.imageDe);
        }
      });
    },
    setImage: function(file, content) {
      if (this.locale === 'EN') {
        this.imageEn = content;
        this.uploadingImageFileEn = file;
        this.deletingImageFileEn = false;
      } else {
        this.imageDe = content;
        this.uploadingImageFileDe = file;
        this.deletingImageFileDe = false;
      }
    },
    deleteImage: function() {
      if (this.locale === 'EN') {
        this.imageEn = null;
        if (this.uploadingImageFileEn) {
          // just remove cache
          this.uploadingImageFileEn = null;
        } else {
          // delete file when submit
          this.deletingImageFileEn = true;
        }
      } else {
        this.imageDe = null;
        if (this.uploadingImageFileDe) {
          // just remove cache
          this.uploadingImageFileDe = null;
        } else {
          // delete file when submit
          this.deletingImageFileDe = true;
        }
      }
    },
    submit: function() {

      if (!this.nameEn) {
        showSnackbar('Englischen Namen eingeben');
        return;
      }

      if (!this.nameDe) {
        showSnackbar('Deutschen Namen eingeben');
        return;
      }

      if (!this.audioDataEn) {
        showSnackbar('Englische Audio-Datei hochladen');
        return;
      }

      if (!this.audioDataDe) {
        showSnackbar('Deutsche Audio-Datei hochladen');
        return;
      }

      this.$emit('submit', {
        audioId: this.audioGroup.id,
        name: {
          en: this.nameEn,
          de: this.nameDe
        },
        text: this.textEn && this.textDe ? { en: this.textEn, de: this.textDe } : null
      }, {
        uploadingAudioFileEn: this.uploadingAudioFileEn,
        uploadingAudioFileDe: this.uploadingAudioFileDe,
        uploadingImageFileEn: this.uploadingImageFileEn,
        uploadingImageFileDe: this.uploadingImageFileDe,
        deletingImageFile: this.deletingImageFileEn || this.deletingImageFileDe,
      });
    }
  },
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value: boolean) {
        this.$emit('update:modelValue', value)
      }
    },
    dialogTitle: function() {
      return this.updating ? 'Audio-Datei bearbeiten' : 'Neue Audio-Datei';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    },
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    },
    hasAudio: function() {
      return (this.audioDataEn && this.locale === 'EN') || (this.audioDataDe && this.locale === 'DE');
    }
  },
  watch: {
    locale: function() {
      this.updateAudioBox();
      this.updateImageBox();
    },
    value: function(newVal) {
      if (!newVal) {
        // stop music player
        this.audioDataEn = null;
        this.audioDataDe = null;
      }
    }
  }
}
</script>