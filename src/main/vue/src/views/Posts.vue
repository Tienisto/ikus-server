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
          :items="channelsWithAll" item-text="name.en" item-value="id"
          return-object
      />

      <LocaleSelector v-model="locale" :locales="locales"/>

      <br>

      <div style="display: flex; align-items: center; justify-content: space-between">
        <span class="mr-4">Angepinnt: </span>
        <v-btn @click="dialogPinnedList = true" text outlined>
          {{ pinned.length }}
        </v-btn>
      </div>

      <br>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading || channels.length === 0">
        <v-icon left>mdi-plus</v-icon>
        Neuer Post
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && posts.length === 0" />

    <Notice v-if="!fetching && channels.length === 0 && posts.length === 0"
            title="Es gibt noch keine Kanäle" info="Bitte erstellen Sie zuerst Kanäle, bevor Sie Posts verfassen." />

    <Notice v-if="!fetching && channels.length !== 0 && posts.length === 0"
            title="Schön leer hier..." info="Sie können rechts ein neuen Post erstellen" />

    <v-card v-for="p in posts" :key="'p-'+p.id" class="mb-4">
      <v-card-text>
        <div style="display: flex; align-items: center">
          <div style="flex: 1">
            <span class="font-weight-bold">{{ localized(p.title) }}</span>
            <br>
            <span>{{ timeString(p.date) }}</span>
          </div>
          <div style="display: flex">

            <v-btn @click="togglePin(p)" icon :disabled="loading">
              <v-icon>{{ p.pinned ? 'mdi-pin' : 'mdi-pin-outline' }}</v-icon>
            </v-btn>

            <v-btn @click="showUpdateDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>

            <v-btn @click="showDeleteDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <PostListDialog v-model="dialogPinnedList" title="Angepinnte Beiträge" info="Sie können wichtige Beiträge anpinnen, so dass diese ganz oben angezeigt werden. Es sollten nicht mehr als 2 sein."
                    :posts="pinned" :loading="loading"
                    @delete="togglePin"/>

    <PostDialog ref="postDialog" v-model="dialogPost" post-type="NEWS" :channels="channels" :locales="locales"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitPost"/>

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
import {createPost, deletePost, getAllNews, getChannels, getNews, togglePinPost, updatePost} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import Notice from "@/components/Notice";
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import {localizedString, showSnackbar} from "@/utils";
import PostDialog from "@/components/dialog/PostDialog";
import LoadingIndicator from "@/components/LoadingIndicator";
import PostListDialog from "@/components/dialog/PostListDialog";

const channelAll = { id: -1, name: { en: 'Alle', de: 'Alle' } };

export default {
  name: 'PostsView',
  components: {PostListDialog, LoadingIndicator, PostDialog, LocaleSelector, GenericDialog, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channelsWithAll: [],
    channels: [],
    channel: {},
    posts: [],
    pinned: [],
    locales: ['EN', 'DE'],
    locale: 'EN',
    dialogPinnedList: false,
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogDelete: false,
    selectedPost: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({ type: 'NEWS' });
      this.channelsWithAll = [ channelAll, ...this.channels ];
      if (!this.channel.id) {
        // init
        this.channel = channelAll;
      }

      let response;
      if (this.channel.id === channelAll.id) {
        response = await getAllNews();
      } else {
        response = await getNews({ channelId: this.channel.id });
      }

      this.posts = response.posts;
      this.pinned = response.pinned;

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
      this.$refs.postDialog.reset(post.channel, this.locale);
      this.$refs.postDialog.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showDeleteDialog: function(post) {
      this.selectedPost = post;
      this.dialogDelete = true;
    },
    submitPost: async function(post) {
      if (this.dialogUpdating)
        await this.updatePost(post);
      else
        await this.createPost(post);
    },
    createPost: async function(post) {
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
    updatePost: async function(post) {
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
    togglePin: async function(post) {
      try {
        this.loading = true;
        await togglePinPost({ postId: post.id });
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