<template>
  <GenericDialog v-model="value" :title="dialogTitle" :width="700" persistent>
    <template v-slot:content>
      <LoadingOverlay v-if="uploading" :text="uploadingText" :percentage="uploadingProgress" :intermediate="uploadIntermediate" />
      <v-row>
        <v-col cols="4">
          <v-select
              :label="channelLabel"
              v-model="channel"
              :items="channels" item-title="name.en" item-value="id"
              return-object
          ></v-select>
        </v-col>
        <v-col cols="8" style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <LocaleSelector v-model="locale" />
        </v-col>
      </v-row>

      <v-card>
        <v-card-text style="padding: 0">
          <RichEditor ref="editorEn" v-show="locale === 'EN'"
                      v-model:title="titleEn" v-model:content="contentEn"
                      :title-placeholder="titlePlaceholder('EN')" :content-placeholder="contentPlaceholder('EN')"
          />
          <RichEditor ref="editorDe" v-show="locale === 'DE'"
                      v-model:title="titleDe" v-model:content="contentDe"
                      :title-placeholder="titlePlaceholder('DE')" :content-placeholder="contentPlaceholder('DE')"
          />
        </v-card-text>
      </v-card>

      <v-card class="mt-8 mb-8 bg-secondary">
        <v-card-title>Bilder</v-card-title>
        <v-card-text>
          <ImageList :files="files" @upload="uploadFiles" @delete="deleteFile" />
        </v-card-text>
      </v-card>

      <DatePicker v-model="date" label="Datum" />

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
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import RichEditor from "@/components/input/RichEditor.vue";
import ImageList from "@/components/ImageList.vue";
import LoadingOverlay from "@/components/LoadingOverlay.vue";
import DatePicker from "@/components/input/DatePicker.vue";
import {uploadPostFile} from "@/api";
import {showSnackbar} from "@/utils/snackbar";
import {sleep} from "@/utils/sleep";
import type {ChannelDto, PostDto, PostFileDto} from "~/models";

export default {
  name: 'PostDialog',
  components: {DatePicker, LoadingOverlay, ImageList, RichEditor, LocaleSelector, GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    postType: {
      type: String,
      required: true
    },
    updating: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
    channels: {
      type: Array,
      required: true
    }
  },
  data: () => ({
    channel: {} as ChannelDto,
    locale: 'EN',
    titleEn: '',
    titleDe: '',
    contentEn: '',
    contentDe: '',
    files: [] as PostFileDto[],
    date: '',
    uploading: false,
    uploadingText: '',
    uploadingProgress: 0,
    uploadIntermediate: false
  }),
  methods: {
    reset: function(channel: ChannelDto, locale: string, date: string) {
      // reset input
      this.titleEn = '';
      this.titleDe = '';
      this.contentEn = '';
      this.contentDe = '';
      this.files = [];

      // apply predefined channel and locale
      this.channel = channel;
      this.locale = locale;
      this.date = date;

      // also reset editor content
      setTimeout(() => {
        this.$refs.editorEn.loadContent('');
        this.$refs.editorDe.loadContent('');
      }, 0);
    },
    load: function(post: PostDto) {
      // apply post
      this.titleEn = post.title.en;
      this.titleDe = post.title.de;
      this.contentEn = post.content.en;
      this.contentDe = post.content.de;
      this.files = [ ...post.files ];
      this.date = post.date;

      setTimeout(() => {
        this.$refs.editorEn.loadContent(post.content.en);
        this.$refs.editorDe.loadContent(post.content.de);
      }, 0);
    },
    uploadFiles: async function(files: PostFileDto[]) {
      try {
        this.uploading = true;
        this.uploadIntermediate = files.length === 1;
        for (let i = 0; i < files.length; i++) {
          this.uploadingText = 'Hochladen (' + i + '/' + files.length + ')';
          this.uploadingProgress = i / files.length;
          const uploaded = await uploadPostFile({ file: files[i] });
          this.files.push(uploaded);
        }
        this.uploadingText = 'Hochladen (' + files.length + '/' + files.length + ')';
        this.uploadingProgress = 1;
        await sleep(300);
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.uploading = false;
      }
    },
    deleteFile: async function(file: PostFileDto) {
      this.files = this.files.filter((f) => f.id !== file.id);
    },
    submit: async function() {

      if (!this.channel.id || this.channel.id < 0) {
        showSnackbar('Bitte Kanal auswählen');
        return;
      }

      if (!this.titleEn) {
        showSnackbar('Englische Überschrift fehlt');
        return false;
      }

      if (!this.titleDe) {
        showSnackbar('Deutsche Überschrift fehlt');
        return false;
      }

      this.$emit('submit', {
        channelId: this.channel.id,
        title: { en: this.titleEn, de: this.titleDe },
        content: { en: this.contentEn, de: this.contentDe },
        files: this.files.map((f) => f.id),
        date: this.date,
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
      if (this.postType === 'NEWS')
        return this.updating ? 'Post bearbeiten' : 'Neuer Post';
      else
        return this.updating ? 'Frage bearbeiten' : 'Neue Frage';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    },
    channelLabel: function() {
      return this.postType === 'NEWS' ? 'Kanal' : 'Gruppe';
    },
    titlePlaceholder: function() {
      if (this.postType === 'NEWS')
        return (locale: string) => locale === 'EN' ? 'Enter title here' : 'Hier Titel eingeben';
      else
        return (locale: string) => locale === 'EN' ? 'Enter question here' : 'Hier Frage eingeben';
    },
    contentPlaceholder: function() {
      if (this.postType === 'NEWS')
        return (locale: string) => locale === 'EN' ? 'Write here your content' : 'Schreiben Sie hier den Inhalt';
      else
        return (locale: string) => locale === 'EN' ? 'Write here your answer' : 'Schreiben Sie hier die Antwort';
    },
  }
}
</script>