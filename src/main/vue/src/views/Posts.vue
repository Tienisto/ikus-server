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
      />

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
              <span class="font-weight-bold">{{ localized(p.title) }}</span>
              <br>
              <span>{{ timeString(p.date) }}</span>
            </div>
            <div style="display: flex">
              <v-btn @click="showUpdateDialog(p)" color="primary">
                <v-icon>mdi-pencil</v-icon>
              </v-btn>

              <v-btn @click="showDeleteDialog(p)" color="primary" class="ml-4">
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </template>

    <LoadingIndicator v-if="fetching" />

    <Notice v-if="!fetching && channels.length === 0 && posts.length === 0"
            title="Es gibt noch keine Kanäle" info="Bitte erstellen Sie zuerst Kanäle, bevor Sie Posts verfassen." />

    <Notice v-if="!fetching && channels.length !== 0 && posts.length === 0"
            title="Schön leer hier..." info="Sie können rechts ein neuen Post erstellen" />

    <PostDialog ref="postDialog" v-model="dialogPost" post-type="NEWS" :channels="channels" :locales="locales"
                :updating="dialogUpdating" :loading="loading"
                @submit="createOrUpdate"/>

    <GenericDialog v-model="dialogDelete" title="Post löschen">
      <template v-slot:content>
        Folgender Beitrag wird gelöscht:
        <br>
        <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
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
import {createPost, deletePost, getChannels, getPosts, updatePost} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import Notice from "@/components/Notice";
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import {localizedString, showSnackbar} from "@/utils";
import PostDialog from "@/components/dialog/PostDialog";
import LoadingIndicator from "@/components/LoadingIndicator";

export default {
  name: 'PostsView',
  components: {LoadingIndicator, PostDialog, LocaleSelector, GenericDialog, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: [],
    channel: {},
    posts: [],
    locales: ['EN', 'DE'],
    locale: 'EN',
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogDelete: false,
    selectedPost: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({ type: 'NEWS' });
      if (!this.channel.id && this.channels.length !== 0) {
        this.channel = this.channels[0];
      }

      if (this.channel.id) {
        this.posts = await getPosts({ channelId: this.channel.id });
      }

      this.fetching = false;
    },
    updateChannel: async function() {
      await this.fetchData();
    },
    showCreateDialog: function() {
      this.$refs.postDialog.reset(this.channel, this.locale);
      this.dialogPost = true;
      this.dialogUpdating = false;
    },
    showUpdateDialog: function(post) {
      // apply post
      this.selectedPost = post;
      this.$refs.postDialog.reset(this.channel, this.locale);
      this.$refs.postDialog.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showDeleteDialog: function(post) {
      this.selectedPost = post;
      this.dialogDelete = true;
    },
    createOrUpdate: async function(post) {
      if (this.dialogUpdating)
        await this.update(post);
      else
        await this.create(post);
    },
    create: async function(post) {
      try {
        this.loading = true;
        await createPost(post);
        this.dialogPost = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    update: async function(post) {
      try {
        this.loading = true;
        await updatePost({
          id: this.selectedPost.id,
          ...post
        });
        this.dialogPost = false;
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
  },
  computed: {
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
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