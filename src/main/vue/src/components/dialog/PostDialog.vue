<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle" :width="700" persistent>
    <template v-slot:content>
      <LoadingOverlay v-if="uploading" :text="uploadingText" :percentage="uploadingProgress" :intermediate="uploadIntermediate" />
      <v-row>
        <v-col cols="4">
          <v-select
              :label="channelLabel"
              v-model="channel"
              :items="channels" item-text="name.en" item-value="id"
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
          <RichEditor ref="editorEn" v-show="locale === 'EN'" :title="titleEn"
                      @change-title="titleEn = $event" @change-content="contentEn = $event"
                      :title-placeholder="titlePlaceholder('EN')" :content-placeholder="contentPlaceholder('EN')"
          />
          <RichEditor ref="editorDe" v-show="locale === 'DE'" :title="titleDe"
                      @change-title="titleDe = $event" @change-content="contentDe = $event"
                      :title-placeholder="titlePlaceholder('DE')" :content-placeholder="contentPlaceholder('DE')"
          />
        </v-card-text>
      </v-card>

      <v-card class="mt-8 mb-8 secondary">
        <v-card-title>Bilder</v-card-title>
        <v-card-text>
          <ImageList :files="files" @upload="uploadFiles" @delete="deleteFile" />
        </v-card-text>
      </v-card>

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
import LocaleSelector from "@/components/LocaleSelector";
import RichEditor from "@/components/RichEditor";
import ImageList from "@/components/ImageList";
import {uploadPostFile} from "@/api";
import {showSnackbar, sleep} from "@/utils";
import LoadingOverlay from "@/components/LoadingOverlay";
export default {
  name: 'PostDialog',
  components: {LoadingOverlay, ImageList, RichEditor, LocaleSelector, GenericDialog},
  props: {
    value: {
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
    channel: {},
    locale: 'EN',
    titleEn: '',
    titleDe: '',
    contentEn: '',
    contentDe: '',
    files: [],
    uploading: false,
    uploadingText: '',
    uploadingProgress: 0,
    uploadIntermediate: false
  }),
  methods: {
    reset: function(channel, locale) {
      // reset input
      this.titleEn = '';
      this.titleDe = '';
      this.contentEn = '';
      this.contentDe = '';
      this.files = [];

      // apply predefined channel and locale
      this.channel = channel;
      this.locale = locale;

      // also reset editor content
      setTimeout(() => {
        this.$refs.editorEn.loadContent('');
        this.$refs.editorDe.loadContent('');
      }, 0);
    },
    load: function(post) {
      // apply post
      this.titleEn = post.title.en;
      this.titleDe = post.title.de;
      this.contentEn = post.content.en;
      this.contentDe = post.content.de;
      this.files = [ ...post.files ];

      setTimeout(() => {
        this.$refs.editorEn.loadContent(post.content.en);
        this.$refs.editorDe.loadContent(post.content.de);
      }, 0);
    },
    uploadFiles: async function(files) {
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
    deleteFile: async function(file) {
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

      await this.$emit('submit', {
        channelId: this.channel.id,
        title: { en: this.titleEn, de: this.titleDe },
        content: { en: this.contentEn, de: this.contentDe },
        files: this.files.map((f) => f.id)
      });
    }
  },
  computed: {
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
        return (locale) => locale === 'EN' ? 'Enter title here' : 'Hier Titel eingeben';
      else
        return (locale) => locale === 'EN' ? 'Enter question here' : 'Hier Frage eingeben';
    },
    contentPlaceholder: function() {
      if (this.postType === 'NEWS')
        return (locale) => locale === 'EN' ? 'Write here your content' : 'Schreiben Sie hier den Inhalt';
      else
        return (locale) => locale === 'EN' ? 'Write here your answer' : 'Schreiben Sie hier die Antwort';
    },
  }
}
</script>