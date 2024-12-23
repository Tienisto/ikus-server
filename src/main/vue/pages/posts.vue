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
      <p class="text-h6 mb-3">Optionen</p>
      <v-select
          label="Kanal" style="width: 250px" variant="underlined"
          v-model="channel" @update:model-value="updateChannel"
          :items="channelsWithAll" item-title="name.en" item-value="id"
          return-object
      />

      <LocaleSelector v-model="locale" />

      <br>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading || channels.length === 0 || isArchivedMode" prepend-icon="mdi-plus">
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
            <v-tooltip text="Bearbeiten" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="showUpdateDialog(p)" :disabled="loading" v-bind="props"
                   icon="mdi-pencil" variant="text" />
              </template>
            </v-tooltip>

            <v-tooltip text="Wiederherstellen" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="showUnarchiveDialog(p)" :disabled="loading" v-bind="props"
                   icon="mdi-restore" variant="text" />
              </template>
            </v-tooltip>

            <v-tooltip text="Löschen" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="showDeleteDialog(p)" :disabled="loading" v-bind="props"
                   icon="mdi-delete" variant="text" />
              </template>
            </v-tooltip>
          </div>

          <div v-else style="display: flex">
            <v-tooltip text="Nach oben" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="moveUp(p)" :disabled="loading || !isAllMode || i === 0" v-bind="props"
                   icon="mdi-arrow-up" variant="text" />
              </template>
            </v-tooltip>

            <v-tooltip text="Nach unten" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="moveDown(p)" :disabled="loading || !isAllMode || i === posts.length - 1" v-bind="props"
                   icon="mdi-arrow-down" variant="text" />
              </template>
            </v-tooltip>

            <v-tooltip text="Bearbeiten" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="showUpdateDialog(p)" :disabled="loading" v-bind="props"
                   icon="mdi-pencil" variant="text" />
              </template>
            </v-tooltip>

            <v-tooltip text="Archivieren" location="top">
              <template v-slot:activator="{ props }">
                <v-btn @click="showArchiveDialog(p)" :disabled="loading" v-bind="props"
                   icon="mdi-delete" variant="text" />
              </template>
            </v-tooltip>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <PostDialog ref="post-dialog" v-model="dialogPost" post-type="NEWS" :channels="channels"
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
        <v-btn @click="dialogArchive = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="toggleArchivePost" color="primary" :loading="loading" prepend-icon="mdi-delete" variant="elevated">
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
        <v-btn @click="dialogUnarchive = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="toggleArchivePost" color="primary" :loading="loading" prepend-icon="mdi-restore" variant="elevated">
          Wiederherstellen
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDeleteDialog v-model="dialogDelete" dialog-title="Post löschen"
                         @delete="deletePost" :loading="loading">
      Folgender Beitrag wird gelöscht:
      <br>
      <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
      <br><br>
      Möchten Sie wirklich diesen Beitrag löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>
  </MainContainer>
</template>

<script lang="ts">
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
import MainContainer from "@/components/layout/MainContainer.vue";
import Notice from "@/components/Notice.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import PostDialog from "@/components/dialog/PostDialog.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import {formatIsoDate, localizedString} from "@/utils/localization";
import {showSnackbar} from "@/utils/snackbar";
import type {ChannelDto, CreatePostDto, MultiLocaleString, PostDto} from "@/models";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";
import {useTemplateRef} from "vue";

const channelAll = {
  id: -1, name: { en: 'Alle', de: 'Alle' }
} as ChannelDto;

const channelArchived = {
  id: -2, name: { en: 'Archiv', de: 'Archiv' }
} as ChannelDto;

export default {
  name: 'PostsView',
  components: {GenericDeleteDialog, LoadingIndicator, PostDialog, LocaleSelector, GenericDialog, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channelsWithAll: [] as ChannelDto[],
    channels: [channelAll] as ChannelDto[],
    channel: {} as ChannelDto,
    posts: [] as PostDto[],
    pinned: [],
    locale: 'EN',
    dialogPinnedList: false,
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogArchive: false,
    dialogUnarchive: false,
    dialogDelete: false,
    selectedPost: {} as PostDto
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({ type: 'NEWS' }) as ChannelDto[];
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
      this.postDialog!.reset(this.channel, this.locale, formatIsoDate(new Date()));
      this.dialogPost = true;
      this.dialogUpdating = false;
    },
    showUpdateDialog: function(post: PostDto) {
      // apply post
      this.selectedPost = post;
      this.postDialog!.reset(post.channel, this.locale, post.date);
      this.postDialog!.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showArchiveDialog: function(post: PostDto) {
      this.selectedPost = post;
      this.dialogArchive = true;
    },
    showUnarchiveDialog: function(post: PostDto) {
      this.selectedPost = post;
      this.dialogUnarchive = true;
    },
    showDeleteDialog: function(post: PostDto) {
      this.selectedPost = post;
      this.dialogDelete = true;
    },
    submitPost: async function(post: CreatePostDto) {
      if (this.dialogUpdating)
        await this.updatePost(post);
      else
        await this.createPost(post);
    },
    createPost: async function(post: CreatePostDto) {
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
    updatePost: async function(post: CreatePostDto) {
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
    moveUp: async function(post: PostDto) {
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
    moveDown: async function(post: PostDto) {
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
      return (obj: MultiLocaleString) => localizedString(obj, this.locale);
    },
    timeString: function() {
      return (time: string) => moment(time).format('ddd, DD.MM.YYYY');
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
  },
  setup() {
    const postDialog = useTemplateRef<InstanceType<typeof PostDialog>>('post-dialog');
    return {postDialog};
  }
}
</script>
