<template>
  <MainContainer title="Posts" icon="mdi-pencil">

    <template v-slot:intro>
      Hier können Sie die Neuigkeiten verwalten.
      <br>
      Alle Posts sind einem Kanal zugeordnet und müssen beidsprachig verfasst werden.
      <br>
      Die Ordnung der Posts kann nur angepasst werden, indem "Alle" als Kanal ausgewählt wird.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <v-select
          label="Kanal" style="width: 250px"
          v-model="channel" @change="updateChannel"
          :items="channelsWithAll" item-text="name.en" item-value="id"
          return-object
      />

      <LocaleSelector v-model="locale" />

      <br>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading || channels.length === 0 || isArchivedMode">
        <v-icon left>mdi-plus</v-icon>
        Neuer Post
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && posts.length === 0" />

    <Notice v-if="!fetching && channels.length === 0 && posts.length === 0"
            title="Es gibt noch keine Kanäle" info="Bitte erstellen Sie zuerst Kanäle, bevor Sie Posts verfassen." />

    <Notice v-if="!fetching && channels.length !== 0 && posts.length === 0"
            title="Schön leer hier..." :info="isArchivedMode ? 'Es wurde noch kein Post archiviert.' : 'Sie können rechts ein neuen Post erstellen.'" />

    <v-card v-for="(p, i) in posts" :key="'p-'+p.id" class="mb-4">
      <v-card-text>
        <div style="display: flex; align-items: center">
          <div style="flex: 1">
            <span class="font-weight-bold">{{ localized(p.title) }}</span>
            <br>
            <span>{{ timeString(p.date) }}</span>
            <span v-if="isAllMode || isArchivedMode"> / {{ localized(p.channel.name) }}</span>
          </div>

          <div v-if="isArchivedMode" style="display: flex">
            <v-btn @click="showUpdateDialog(p)" icon :disabled="loading">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn @click="showUnarchiveDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-restore</v-icon>
            </v-btn>
            <v-btn @click="showDeleteDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </div>

          <div v-else style="display: flex">
            <v-btn @click="moveUp(p)" icon :disabled="loading || !isAllMode || i === 0">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>

            <v-btn @click="moveDown(p)" class="ml-4" icon :disabled="loading || !isAllMode || i === posts.length - 1">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>

            <v-btn @click="showUpdateDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>

            <v-btn @click="showArchiveDialog(p)" class="ml-4" icon :disabled="loading">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <PostDialog ref="postDialog" v-model="dialogPost" post-type="NEWS" :channels="channels"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitPost"/>

    <GenericDialog v-model="dialogArchive" title="Post archivieren">
      <template v-slot:content>
        Folgender Beitrag wird archiviert:
        <br>
        <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
        <br><br>
        Sie können diesen Beitrag jederzeit wiederherstellen.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogArchive = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="toggleArchivePost" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Archivieren
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogUnarchive" title="Post wiederherstellen">
      <template v-slot:content>
        Folgender Beitrag wird wiederhergestellt:
        <br>
        <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
        <br><br>
        Dieser Beitrag wird in die oberste Position eingeführt. Fortfahren?
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogUnarchive = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="toggleArchivePost" color="primary" :loading="loading">
          <v-icon left>mdi-restore</v-icon>
          Wiederherstellen
        </v-btn>
      </template>
    </GenericDialog>

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
import {
  createPost,
  deletePost,
  getAllNews, getArchivedNews,
  getChannels,
  getNews,
  moveDownPost,
  moveUpPost, toggleArchive,
  updatePost
} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import Notice from "@/components/Notice";
import GenericDialog from "@/components/dialog/GenericDialog";
import LocaleSelector from "@/components/LocaleSelector";
import {localizedString, showSnackbar} from "@/utils";
import PostDialog from "@/components/dialog/PostDialog";
import LoadingIndicator from "@/components/LoadingIndicator";

const channelAll = { id: -1, name: { en: 'Alle', de: 'Alle' } };
const channelArchived = { id: -2, name: { en: 'Archiv', de: 'Archiv' } };

export default {
  name: 'PostsView',
  components: {LoadingIndicator, PostDialog, LocaleSelector, GenericDialog, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channelsWithAll: [],
    channels: [],
    channel: {},
    posts: [],
    pinned: [],
    locale: 'EN',
    dialogPinnedList: false,
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogArchive: false,
    dialogUnarchive: false,
    dialogDelete: false,
    selectedPost: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({ type: 'NEWS' });
      this.channelsWithAll = [ channelAll, ...this.channels, channelArchived ];
      if (!this.channel.id) {
        // init
        this.channel = channelAll;
      }

      let response;
      if (this.channel.id === channelAll.id) {
        response = await getAllNews();
      } else if (this.channel.id === channelArchived.id) {
        response = await getArchivedNews();
      } else {
        response = await getNews({ channelId: this.channel.id });
      }

      this.posts = response;
      this.fetching = false;
    },
    updateChannel: async function() {
      this.posts = [];
      await this.fetchData();
    },
    showCreateDialog: function() {
      this.$refs.postDialog.reset(this.channel, this.locale, new Date().toISOString().substring(0, 10));
      this.dialogPost = true;
      this.dialogUpdating = false;
    },
    showUpdateDialog: function(post) {
      // apply post
      this.selectedPost = post;
      this.$refs.postDialog.reset(post.channel, this.locale, post.date);
      this.$refs.postDialog.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showArchiveDialog: function(post) {
      this.selectedPost = post;
      this.dialogArchive = true;
    },
    showUnarchiveDialog: function(post) {
      this.selectedPost = post;
      this.dialogUnarchive = true;
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
    moveUp: async function(post) {
      try {
        this.loading = true;
        await moveDownPost({ id: post.id });
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDown: async function(post) {
      try {
        this.loading = true;
        await moveUpPost({ id: post.id });
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
    toggleArchivePost: async function() {
      try {
        this.loading = true;
        await toggleArchive({ postId: this.selectedPost.id });
        this.dialogArchive = false;
        this.dialogUnarchive = false;
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
    isAllMode: function() {
      return this.channel.id === channelAll.id;
    },
    isArchivedMode: function() {
      return this.channel.id === channelArchived.id;
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>