<template>
  <div>
    <LoadingOverlay v-if="uploading" text="Hochladen..." intermediate />
    <v-card>
      <v-card-title>
        <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
          <span style="display: flex; align-items: center">
            <v-icon class="mr-2">mdi-file-document</v-icon>
            PDF-Datei ({{ title }})
          </span>
          <FileUpload v-slot:default="{ upload }" @select="uploadHandbook">
            <v-btn @click="upload" color="primary" text="Hochladen" prepend-icon="mdi-upload" size="small" />
          </FileUpload>
        </div>
      </v-card-title>
      <v-card-text>
        <div v-if="showPdf" style="width: 100%; height: 400px; border: 1px solid #eaeaea">
          <object :data="pdfUrl" type="application/pdf" style="width: 100%; height: 100%">
            PDF konnte nicht geladen werden.
          </object>
        </div>
        <div v-else style="height: 400px"></div>
      </v-card-text>
    </v-card>

    <v-card class="mt-6">
      <v-card-title>
        <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
          <span style="display: flex; align-items: center">
            <v-icon class="mr-2">mdi-format-list-bulleted-square</v-icon>
            Inhaltsverzeichnis ({{ title }})
          </span>
          <span>
            <v-btn @click="save" color="primary" text="Speichern" prepend-icon="mdi-content-save" size="small"
                   v-bind="props" />
          </span>
        </div>
      </v-card-title>

      <v-card-text>
        <SimpleTextArea v-model="bookmarksRaw" style="height: 400px; border: 1px solid #eaeaea" :placeholder="bookmarkPlaceholder" />
      </v-card-text>
    </v-card>
  </div>

</template>

<script lang="ts">
import FileUpload from "@/components/FileUpload";
import {getFileUrl, updateBookmarks, uploadHandbook} from "@/api";
import SimpleTextArea from "@/components/input/SimpleTextArea";
import LoadingOverlay from "@/components/LoadingOverlay.vue";
import {showSnackbar} from "@/utils/snackbar";
import {sleep} from "@/utils/sleep";
export default {
  name: 'HandbookComponent',
  components: {LoadingOverlay, SimpleTextArea, FileUpload},
  props: {
    title: {
      type: String,
      required: true
    },
    locale: {
      type: String,
      required: false
    },
    bookmarks: {
      type: Array,
      required: true
    }
  },
  data: () => ({
    loading: false,
    uploading: false,
    showPdf: true,
    bookmarksRaw: ''
  }),
  methods: {
    uploadHandbook: async function(file) {
      try {
        this.uploading = true;
        await uploadHandbook({ file, locale: this.locale });
        await this.reloadPdf();
      } catch (e) {
        if (e === 409)
          showSnackbar('Nur PDF-Dateien erlaubt');
        else
          showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.uploading = false;
      }
    },
    reloadPdf: async function() {
      this.showPdf = false;
      await sleep(100);
      this.showPdf = true;
    },
    save: async function() {

      const bookmarks = this.bookmarksRaw
          .split('\n')
          .map((row) => {
            const indexComma = row.indexOf(',');
            if (indexComma === -1 || indexComma === row.length - 1)
              return null;

            const page = parseInt(row.substring(0, indexComma));

            if (isNaN(page))
              return null;

            const name = row.substring(indexComma + 1);
            return { page, name };
          })
          .filter((bookmark) => bookmark);

      try {
        this.loading = true;
        await updateBookmarks({ bookmarks, locale: this.locale });
        this.$emit('update');
        showSnackbar('Inhaltsverzeichnis gespeichert.')
      } catch (e) {
        if (e === 409)
          showSnackbar('Nur PDF-Dateien erlaubt');
        else
          showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    }
  },
  computed: {
    pdfUrl: function() {
      return getFileUrl('handbook/' + this.locale.toLowerCase() + '.pdf');
    },
    bookmarkPlaceholder: function() {
      return 'Format: <Seite>, <Überschrift>\nBeispiel 1: 6, Ansprechpartner\nBeispiel 2: 11, 2. Ansprechpartner'
    }
  },
  watch: {
    bookmarks: {
      immediate: true,
      handler: function(newVal) {
        this.bookmarksRaw = newVal.map(row => row.page + ', ' + row.name).join('\n');
      }
    }
  }
}
</script>
