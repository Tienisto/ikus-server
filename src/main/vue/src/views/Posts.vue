<template>
  <MainContainer title="Posts" icon="mdi-pencil">

    <template v-slot:intro>
      Hier können Sie die Neuigkeiten verwalten.
      <br>
      Alle Posts sind einem Kanal zugeordnet und müssen beidsprachig verfasst werden.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <v-select
          label="Kanal" style="width: 250px"
          v-model="channel" @change="updateChannel"
          :items="channels" item-text="name.en" item-value="id"
          return-object
      ></v-select>

      <LocaleSelector v-model="locale" :locales="locales"/>

      <br>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading || channels.length === 0">
        <v-icon left>mdi-plus</v-icon>
        Neuer Post
      </v-btn>
    </template>

    <template v-if="!fetching">
      <v-card v-for="p in posts" :key="'p-'+p.id" class="mb-4">
        <v-card-text>
          <div style="display: flex; align-items: center">
            <div style="flex: 1">
              <span class="font-weight-bold">{{ title(p.title) }}</span>
              <br>
              <span>{{ timeString(p.date) }}</span>
            </div>
            <div style="display: flex">
              <v-btn @click="showUpdateDialog(p)" elevation="2" color="primary">
                <v-icon>mdi-pencil</v-icon>
              </v-btn>

              <v-btn @click="showDeleteDialog(p)" elevation="2" color="primary" class="ml-4">
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </template>

    <v-progress-circular v-if="fetching" color="primary" indeterminate />

    <Notice v-if="!fetching && channels.length === 0 && posts.length === 0"
            title="Es gibt noch keine Kanäle" info="Bitte erstellen Sie zuerst Kanäle, bevor Sie Posts verfassen." />

    <Notice v-if="!fetching && channels.length !== 0 && posts.length === 0"
            title="Schön leer hier..." info="Sie können rechts ein neuen Post erstellen" />

    <GenericDialog v-model="dialogCreateUpdate" :title="dialogUpdating ? 'Post bearbeiten' : 'Neuer Post'" :width="700">
      <template v-slot:content>
        <v-row>
          <v-col cols="4">
            <v-select
                label="Kanal"
                v-model="postChannel"
                :items="channels" item-text="name.en" item-value="id"
                return-object
            ></v-select>
          </v-col>
          <v-col cols="8" style="display: flex; align-items: center">
            <div style="flex: 1"></div>
            <LocaleSelector v-model="postLocale" :locales="locales" />
          </v-col>
        </v-row>

        <v-card>
          <v-card-text style="padding: 0">
            <RichEditor ref="editorEn" v-show="postLocale === 'EN'" :title="titleEn"
                        @change-title="titleEn = $event" @change-content="contentEn = $event"
                        title-placeholder="Enter title here" content-placeholder="Write here your content"
            />
            <RichEditor ref="editorDe" v-show="postLocale === 'DE'" :title="titleDe"
                        @change-title="titleDe = $event" @change-content="contentDe = $event"
                        title-placeholder="Hier Titel eingeben" content-placeholder="Schreiben Sie hier den Inhalt"
            />
          </v-card-text>
        </v-card>

        <v-card class="mt-8 mb-8">
          <v-card-text>
            <p class="text-h6">Bilder</p>

            <ImageList :files="files" @upload="uploadFiles" @delete="deleteFile" />
          </v-card-text>
        </v-card>


      </template>

      <template v-slot:actions>
        <v-btn @click="dialogCreateUpdate = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="dialogUpdating ? update() : create()" color="primary" :loading="loading">
          <v-icon left>{{ dialogUpdating ? 'mdi-content-save' : 'mdi-plus' }}</v-icon>
          {{ dialogUpdating ? 'Speichern' : 'Erstellen' }}
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogDelete" title="Post löschen">
      <template v-slot:content>
        Folgender Beitrag wird gelöscht:
        <br>
        <b>{{ titleEn }}</b>
        <br><br>
        Möchten Sie wirklich diesen Beitrag löschen?
        <br>
        Dieser Vorgang ist nicht widerrufbar.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDelete = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deletePost" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script>
import moment from "moment"
import {createPost, deletePost, deletePostFile, getChannels, getPosts, updatePost, uploadPostFile} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import Notice from "@/components/Notice";
import GenericDialog from "@/components/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import RichEditor from "@/components/RichEditor";
import {showSnackbar} from "@/utils";
import ImageList from "@/components/ImageList";

export default {
  name: 'PostsView',
  components: {ImageList, RichEditor, LocaleSelector, GenericDialog, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: [],
    channel: {},
    posts: [],
    locales: ['EN', 'DE'],
    locale: 'EN',
    dialogCreateUpdate: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogDelete: false,
    selectedPost: {},
    postLocale: 'EN',
    postChannel: {},
    titleEn: '',
    titleDe: '',
    contentEn: '',
    contentDe: '',
    files: []
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({ type: 'NEWS' });
      if (!this.channel.id && this.channels.length !== 0) {
        this.channel = this.channels[0];
        this.postChannel = this.channels[0];
      }

      if (this.channel.id) {
        this.posts = await getPosts({ channelId: this.channel.id });
      }

      this.fetching = false;
    },
    updateChannel: async function() {
      await this.fetchData();
    },
    resetDialogData: function() {
      this.titleEn = '';
      this.titleDe = '';
      this.contentEn = '';
      this.contentDe = '';

      // apply global channel and locale
      this.postChannel = this.channel;
      this.postLocale = this.locale;
    },
    showCreateDialog: function() {
      this.resetDialogData();
      this.dialogCreateUpdate = true;
      this.dialogUpdating = false;
      setTimeout(() => {
        this.$refs.editorEn.loadContent('');
        this.$refs.editorDe.loadContent('');
      }, 0);
    },
    showUpdateDialog: function(post) {
      this.resetDialogData();
      // apply post
      this.selectedPost = post;
      this.titleEn = post.title.en;
      this.titleDe = post.title.de;
      this.contentEn = post.content.en;
      this.contentDe = post.content.de;
      // open
      this.dialogCreateUpdate = true;
      this.dialogUpdating = true;
      // load editor content
      setTimeout(() => {
        this.$refs.editorEn.loadContent(post.content.en);
        this.$refs.editorDe.loadContent(post.content.de);
      }, 0);
    },
    showDeleteDialog: function(post) {
      this.resetDialogData();
      this.selectedPost = post;
      this.titleEn = post.title.en;
      this.dialogDelete = true;
    },
    validateDialog: function() {
      if (!this.titleEn || !this.titleDe) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return false;
      }
      return true;
    },
    create: async function() {

      if(!this.validateDialog())
        return;

      try {
        this.loading = true;
        await createPost({
          channelId: this.postChannel.id,
          title: { en: this.titleEn, de: this.titleDe },
          content: { en: this.contentEn, de: this.contentDe }
        });
        this.dialogCreateUpdate = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    update: async function() {
      if(!this.validateDialog())
        return;

      try {
        this.loading = true;
        await updatePost({
          id: this.selectedPost.id,
          channelId: this.postChannel.id,
          title: { en: this.titleEn, de: this.titleDe },
          content: { en: this.contentEn, de: this.contentDe }
        });
        this.dialogCreateUpdate = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deletePost: async function() {
      try {
        this.loading = true;
        await deletePost({ id: this.selectedPost.id });
        this.dialogDelete = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    uploadFiles: async function(files) {
      for (const file of files) {
        try {
          const uploaded = await uploadPostFile({ file });
          this.files.push(uploaded);
        } catch (e) {
          showSnackbar('Ein Fehler ist aufgetreten');
        }
      }
    },
    deleteFile: async function(file) {
      try {
        await deletePostFile({ fileId: file.id });
        this.files = this.files.filter((f) => f.id !== file.id);
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      }
    }
  },
  computed: {
    title: function() {
      return (title) => this.locale === 'EN' ? title.en : title.de;
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    },
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>