<template>
  <GenericDialog v-model="value" :title="dialogTitle">
    <template v-slot:content>
      <v-row>
        <v-col cols="6" style="display: flex; flex-direction: column;">

          <div style="flex: 1"></div>
          <LocaleSelector v-model="locale" class="mb-4" />
          <v-text-field v-model="nameEn" label="Name (englisch)" v-if="locale === 'EN'" :disabled="loading" filled hide-details />
          <v-text-field v-model="nameDe" label="Name (deutsch)" v-else :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-card class="secondary" style="height: 100%">
            <v-card-text class="pa-1" style="height: 100%; min-height: 210px">
              <ImagePicker ref="image-picker" :width="200" :height="200" @select="setImage" @delete="deleteImage"/>
            </v-card-text>
          </v-card>
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

<script lang="ts">
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import ImagePicker from "@/components/input/ImagePicker";
import {getFileUrl} from "@/api";
import {showSnackbar} from "@/utils/snackbar";
import {useTemplateRef} from "vue";
import GroupDialog from "~/components/dialog/GroupDialog.vue";

export default {
  name: 'AudioDialog',
  components: {ImagePicker, LocaleSelector, GenericDialog},
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
    locale: 'EN',
    nameEn: '',
    nameDe: '',
    uploadingFileEn: null,
    uploadingFileDe: null,
    deletingFileEn: false,
    deletingFileDe: false,
    imageEn: null, // display purpose only
    imageDe: null // display purpose only
  }),
  methods: {
    reset: function(locale) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.uploadingFileEn = null;
      this.uploadingFileDe = null;
      this.deletingFileEn = false;
      this.deletingFileDe = false;
      this.imageEn = null;
      this.imageDe = null;

      // apply preset
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

      if (audio.image) {
        this.imageEn = getFileUrl(audio.image.en) + '#' + new Date().getTime();
        this.imageDe = getFileUrl(audio.image.de) + '#' + new Date().getTime();

        this.$nextTick(() => {
          this.updateImageBox();
        });
      }
    },
    updateImageBox: function() {
      if (this.locale === 'EN') {
        this.imagePicker?.setImage(this.imageEn);
      } else {
        this.imagePicker?.setImage(this.imageDe);
      }
    },
    setImage: function(file, content) {
      if (this.locale === 'EN') {
        this.imageEn = content;
        this.uploadingFileEn = file;
        this.deletingFileEn = false;
      } else {
        this.imageDe = content;
        this.uploadingFileDe = file;
        this.deletingFileDe = false;
      }
    },
    deleteImage: function() {
      if (this.locale === 'EN') {
        this.imageEn = null;
        if (this.uploadingFileEn) {
          // just remove cache
          this.uploadingFileEn = null;
        } else {
          // delete file when submit
          this.deletingFileEn = true;
        }
      } else {
        this.imageDe = null;
        if (this.uploadingFileDe) {
          // just remove cache
          this.uploadingFileDe = null;
        } else {
          // delete file when submit
          this.deletingFileDe = true;
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

      this.$emit('submit', {
        name: {
          en: this.nameEn,
          de: this.nameDe
        }
      }, {
        uploadingFileEn: this.uploadingFileEn,
        uploadingFileDe: this.uploadingFileDe,
        deletingFile: this.deletingFileEn || this.deletingFileDe,
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
      return this.updating ? 'Audio bearbeiten' : 'Neue Audio-Gruppe';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    }
  },
  watch: {
    locale: function() {
      this.updateImageBox();
    }
  },
  setup() {
    const imagePicker = useTemplateRef<InstanceType<typeof ImagePicker>>('image-picker');
    return {imagePicker};
  }
}
</script>