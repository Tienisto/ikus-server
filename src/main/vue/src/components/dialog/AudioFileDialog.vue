<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
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

      <div style="display: flex; align-items: center" class="mt-2">
        <div style="flex: 1" class="pr-2">
          <audio ref="audio" v-if="hasAudio" controls="controls" style="width: 100%; outline: none">
            <source :src="locale === 'EN' ? contentEn : contentDe" />
          </audio>
          <span v-else>Bitte rechts eine Audio-Datei hochladen.</span>
        </div>
        <FileUpload v-slot:default="{ upload }" @select-with-content="setFile">
          <v-tooltip top>
            <template v-slot:activator="{ on, attrs }">
              <v-btn @click="upload" color="primary" v-bind="attrs" v-on="on">
                <v-icon>mdi-upload</v-icon>
              </v-btn>
            </template>
            <span>Datei hochladen</span>
          </v-tooltip>
        </FileUpload>
      </div>

      <v-text-field v-model="nameEn" label="Name" v-if="locale === 'EN'" class="mt-4" :disabled="loading" filled hide-details />
      <v-text-field v-model="nameDe" label="Name" v-else class="mt-4" :disabled="loading" filled hide-details />

      <v-textarea v-model="textEn" label="Text (optional)" v-if="locale === 'EN'" class="mt-4" :disabled="loading" :rows="6" filled hide-details />
      <v-textarea v-model="textDe" label="Text (optional)" v-else class="mt-4" :disabled="loading" :rows="6" filled hide-details />
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading">
        <v-icon left>{{ submitIcon }}</v-icon>
        {{ submitText }}
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";
import {localizedString, showSnackbar} from "@/utils";
import LocaleSelector from "@/components/LocaleSelector";
import {getFileUrl} from "@/api";
import FileUpload from "@/components/FileUpload";

export default {
  name: 'AudioFileDialog',
  components: {FileUpload, LocaleSelector, GenericDialog},
  props: {
    value: {
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
    uploadingFileEn: null,
    uploadingFileDe: null,
    contentEn: null, // display purpose only
    contentDe: null // display purpose only
  }),
  methods: {
    reset: function(audioGroup, locale) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.textEn = '';
      this.textDe = '';
      this.uploadingFileEn = null;
      this.uploadingFileDe = null;
      this.contentEn = null;
      this.contentDe = null;

      // apply preset
      this.audioGroup = audioGroup;
      this.locale = locale;
    },
    load: function(audio) {
      // apply
      this.nameEn = audio.name.en;
      this.nameDe = audio.name.de;
      this.textEn = audio.text ? audio.text.en : '';
      this.textDe = audio.text ? audio.text.de : '';
      this.contentEn = getFileUrl(audio.file.en) + '#' + new Date().getTime();
      this.contentDe = getFileUrl(audio.file.de) + '#' + new Date().getTime();
      this.updateAudioBox();
    },
    updateAudioBox: function() {
      this.$nextTick(() => {
        if (this.hasAudio)
          this.$refs.audio.load();
      });
    },
    setFile: function(file, content) {
      if (this.locale === 'EN') {
        this.contentEn = content;
        this.uploadingFileEn = file;
      } else {
        this.contentDe = content;
        this.uploadingFileDe = file;
      }
      this.updateAudioBox();
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

      if (!this.contentEn) {
        showSnackbar('Englische Audio-Datei hochladen');
        return;
      }

      if (!this.contentDe) {
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
        uploadingFileEn: this.uploadingFileEn,
        uploadingFileDe: this.uploadingFileDe
      });
    }
  },
  computed: {
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
      return (this.contentEn && this.locale === 'EN') || (this.contentDe && this.locale === 'DE');
    }
  },
  watch: {
    locale: function() {
      this.updateAudioBox();
    },
    value: function(newVal) {
      if (!newVal) {
        // stop music player
        this.contentEn = null;
        this.contentDe = null;
      }
    }
  }
}
</script>