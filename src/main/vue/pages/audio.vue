<template>
  <MainContainer title="Audio-Guides" icon="mdi-headphones">

    <template v-slot:intro>
      Hier können Sie die Audio-Dateien verwalten.
      <br>
      Jede Gruppe besteht aus einem Titelbild sowie aus beliebig vielen Audiodateien.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>
      <LocaleSelector v-model="locale" />
      <br>
      <v-btn @click="showCreateAudio" color="primary" block :disabled="loading" prepend-icon="mdi-plus">
        Neue Gruppe
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && audioList.length === 0" />

    <Notice v-if="!fetching && audioList.length === 0"
            title="Es existieren noch keine Audio-Gruppen" info="Sie können rechts eine neue Gruppe erstellen" />

    <AudioCard v-for="a in audioList" :key="a.id"
               :loading="loading" :audio="a" :locale="locale"
               @move-up-group="moveUpAudio(a)" @move-down-group="moveDownAudio(a)"
               @update-group="showUpdateAudio(a)" @delete-group="showDeleteAudio(a)"
               @create-file="showCreateAudioFile(a)" @update-file="showUpdateAudioFile" @delete-file="showDeleteAudioFile"
               @move-up-file="moveUpAudioFile" @move-down-file="moveDownAudioFile"
               class="mb-6" />

    <!-- DIALOGS -->

    <AudioDialog ref="audioDialog" v-model="dialogAudio" :loading="loading" :updating="dialogUpdating"
                 @submit="submitAudio"/>

    <AudioFileDialog ref="audioFileDialog" v-model="dialogAudioFile" :loading="loading" :updating="dialogUpdating"
                     @submit="submitAudioFile"/>

    <GenericDeleteDialog v-model="dialogDeleteAudioFile" dialog-title="Audio-Datei löschen"
                         @delete="deleteAudioFile" :loading="loading">
      Folgende Datei wird gelöscht:
      <br>
      <b>{{ selectedAudioFile.name ? selectedAudioFile.name.en : '' }}</b>
      <br><br>
      Möchten Sie wirklich diese Datei löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar und betrifft beide Sprachen.
    </GenericDeleteDialog>

    <ConfirmTextDialog ref="deleteAudioDialog" v-model="dialogDeleteAudio" :confirm-text="confirmText" :loading="loading" title="Gruppe löschen"
                       @submit="deleteAudio">
      Möchten Sie wirklich die Audio-Gruppe {{ confirmText }} löschen?
      <br>
      Bitte beachten Sie, dass <b>alle</b> Dateien gelöscht werden, die der Gruppe zugeordnet sind.
      <br>
      Schreiben Sie in dem Feld "{{ confirmText }}", um diese Audio-Gruppe zu löschen.
    </ConfirmTextDialog>

  </MainContainer>
</template>

<script lang="ts">
import {
  getAudio,
  moveUpAudio,
  moveDownAudio,
  deleteAudio,
  createAudioFile,
  updateAudioFile,
  moveUpAudioFile,
  moveDownAudioFile,
  deleteAudioFile,
  createAudio,
  updateAudio,
  setAudioImage, deleteAudioImage, uploadAudioFileAudio, uploadAudioFileImage, deleteAudioFileImage
} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import LoadingIndicator from "@/components/LoadingIndicator";
import Notice from "@/components/Notice";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import AudioCard from "@/components/cards/AudioCard";
import AudioDialog from "@/components/dialog/AudioDialog";
import AudioFileDialog from "@/components/dialog/AudioFileDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import {localizedString} from "~/utils/localization";
import {showSnackbar} from "~/utils/snackbar";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";
import type {AudioDto} from "~/models";

export default {
  name: 'AudioView',
  components: {
    GenericDeleteDialog,
    GenericDialog,
    AudioFileDialog,
    AudioDialog,
    AudioCard,
    ConfirmTextDialog, Notice, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    audioList: [] as AudioDto[],
    locale: 'EN',
    dialogAudio: false, // for audio group
    dialogAudioFile: false, // for audio file
    dialogUpdating: false, // true if dialog is used for updating an audio group OR an audio file
    dialogDeleteAudio: false,
    dialogDeleteAudioFile: false,
    selectedAudio: {},
    selectedAudioFile: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.audioList = await getAudio();
      this.fetching = false;
    },
    showCreateAudio: function() {
      this.$refs.audioDialog.reset(this.locale);
      this.dialogUpdating = false;
      this.dialogAudio = true;
    },
    showUpdateAudio: function(audio) {
      this.$refs.audioDialog.reset(this.locale);
      this.$refs.audioDialog.load(audio);
      this.selectedAudio = audio;
      this.dialogUpdating = true;
      this.dialogAudio = true;
    },
    showDeleteAudio: function(audio) {
      this.$refs.deleteAudioDialog.reset();
      this.selectedAudio = audio;
      this.dialogDeleteAudio = true;
    },
    showCreateAudioFile: function(audio) {
      this.$refs.audioFileDialog.reset(audio, this.locale);
      this.dialogUpdating = false;
      this.dialogAudioFile = true;
    },
    showUpdateAudioFile: function(audio, audioFile) {
      this.$refs.audioFileDialog.reset(audio, this.locale);
      this.$refs.audioFileDialog.load(audioFile);
      this.selectedAudioFile = audioFile;
      this.dialogUpdating = true;
      this.dialogAudioFile = true;
    },
    showDeleteAudioFile: function(audio, audioFile) {
      this.selectedAudioFile = audioFile;
      this.dialogDeleteAudioFile = true;
    },
    submitAudio: async function(audio, actions) {
      if (this.dialogUpdating)
        await this.updateAudio(audio, actions);
      else
        await this.createAudio(audio, actions);
    },
    createAudio: async function(audio, actions) {
      try {
        this.loading = true;
        const savedAudio = await createAudio(audio);
        await this.handleAudioGroupActions(savedAudio.id, actions);
        this.dialogAudio = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateAudio: async function(audio, actions) {
      try {
        this.loading = true;
        await updateAudio({ id: this.selectedAudio.id, ...audio });
        await this.handleAudioGroupActions(this.selectedAudio.id, actions);
        this.dialogAudio = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    handleAudioGroupActions: async function(audioId, actions) {
      if (actions.uploadingFileEn) {
        await setAudioImage({ audioId, file: actions.uploadingFileEn, locale: 'EN' });
      }

      if (actions.uploadingFileDe) {
        await setAudioImage({ audioId, file: actions.uploadingFileDe, locale: 'DE' });
      }

      if (actions.deletingFile) {
        await deleteAudioImage({ audioId });
      }
    },
    moveUpAudio: async function(audio) {
      try {
        this.loading = true;
        await moveUpAudio({id: audio.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownAudio: async function(audio) {
      try {
        this.loading = true;
        await moveDownAudio({id: audio.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteAudio: async function() {
      try {
        this.loading = true;
        await deleteAudio({ id: this.selectedAudio.id });
        this.dialogDeleteAudio = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    submitAudioFile: async function(audioFile, actions) {
      if (this.dialogUpdating)
        await this.updateAudioFile(audioFile, actions);
      else
        await this.createAudioFile(audioFile, actions);
    },
    createAudioFile: async function(audioFile, actions) {
      try {
        this.loading = true;
        const response = await createAudioFile(audioFile);
        await this.handleAudioFileActions(null, response.token, actions);
        this.dialogAudioFile = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateAudioFile: async function(audioFile, actions) {
      try {
        this.loading = true;
        await updateAudioFile({
          id: this.selectedAudioFile.id,
          ...audioFile
        });
        await this.handleAudioFileActions(this.selectedAudioFile.id, null, actions);
        this.dialogAudioFile = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    handleAudioFileActions: async function(fileId, token, actions) {
      if (actions.uploadingAudioFileEn) {
        const response = await uploadAudioFileAudio({ fileId, token, file: actions.uploadingAudioFileEn, locale: 'EN' });
        if (response && response.id) {
          // just created after last request
          fileId = response.id;
        }
      }

      if (actions.uploadingAudioFileDe) {
        const response = await uploadAudioFileAudio({ fileId, token, file: actions.uploadingAudioFileDe, locale: 'DE' });
        if (response && response.id) {
          // just created after last request
          fileId = response.id;
        }
      }

      if (actions.uploadingImageFileEn) {
        await uploadAudioFileImage({ fileId, file: actions.uploadingImageFileEn, locale: 'EN' });
      }

      if (actions.uploadingImageFileDe) {
        await uploadAudioFileImage({ fileId, file: actions.uploadingImageFileDe, locale: 'DE' });
      }

      if (actions.deletingImageFile) {
        await deleteAudioFileImage({ fileId });
      }
    },
    moveUpAudioFile: async function(audio, audioFile) {
      try {
        this.loading = true;
        await moveUpAudioFile({id: audioFile.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownAudioFile: async function(audio, audioFile) {
      try {
        this.loading = true;
        await moveDownAudioFile({id: audioFile.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteAudioFile: async function() {
      try {
        this.loading = true;
        await deleteAudioFile({ id: this.selectedAudioFile.id });
        this.dialogDeleteAudioFile = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
  },
  computed: {
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    },
    confirmText: function() {
      return this.selectedAudio.name ? this.selectedAudio.name.de : '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>
