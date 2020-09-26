<template>
  <MainContainer title="FAQ" icon="mdi-help-circle">

    <template v-slot:intro>
      Hier können Sie häufig gestellte Fragen verwalten.
      <br>
      Zu jeder Frage können Sie einen Beitrag verfassen (inkl. Bilder).
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" :locales="locales"/>
      <br>
      <v-btn @click="showCreateChannel" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neue Gruppe
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching" />

    <Notice v-if="!fetching && groups.length === 0"
            title="Es existieren noch keine Gruppen" info="Sie können rechts eine neue Gruppe erstellen" />

    <div v-if="!fetching">
      <ListCard v-for="g in groups" :key="g.channel.id"
                :title="localized(g.channel.name)" :items="g.posts" :empty-notice="!fetching && g.posts.length === 0" empty-notice-text="Noch keine Fragen."
                @edit="showUpdateChannel(g.channel)" @delete="showDeleteChannel(g.channel)" @create="showCreatePost(g.channel)"
                class="mb-6">

        <template v-slot:item="{ item }">
          <span class="">{{ localized(item.title) }}</span>
        </template>

        <template v-slot:actions="{ item }">
          <v-btn @click="showUpdatePost(item)" icon small>
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn @click="showDeletePost(item)" class="ml-2" icon small>
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
      </ListCard>
    </div>

    <!-- DIALOGS -->

    <PostDialog ref="postDialog" v-model="dialogPost" post-type="FAQ" :channels="channels" :locales="locales"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitPost"/>

    <GenericDialog v-model="dialogDeletePost" title="Frage löschen">
      <template v-slot:content>
        Folgende Frage wird gelöscht:
        <br>
        <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
        <br><br>
        Möchten Sie wirklich diese Frage löschen?
        <br>
        Dieser Vorgang ist nicht widerrufbar.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDeletePost = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deletePost" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

    <GroupDialog ref="channelDialog" v-model="dialogChannel" :dialog-title="dialogUpdating ? 'Gruppe umbenennen' : 'Neue Gruppe'" :updating="dialogUpdating" :loading="loading"
                  @submit="submitChannel"/>

    <ConfirmTextDialog ref="deleteChannelDialog" v-model="dialogDeleteChannel" :confirm-text="confirmText" :loading="loading" title="Gruppe löschen"
                       @submit="deleteChannel">
      Möchten Sie wirklich die Gruppe {{ confirmText }} löschen?
      <br>
      Bitte beachten Sie, dass <b>alle</b> Fragen gelöscht werden, die der Gruppe zugeordnet sind.
      <br>
      Schreiben Sie in dem Feld "{{ confirmText }}", um diesen Kanal zu löschen.
    </ConfirmTextDialog>

  </MainContainer>
</template>

<script>
import moment from "moment";
import {createChannel, createPost, deleteChannel, deletePost, getPostsGrouped, renameChannel, updatePost} from "@/api";
import {localizedString, showSnackbar} from "@/utils";
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import LoadingIndicator from "@/components/LoadingIndicator";
import PostDialog from "@/components/dialog/PostDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import Notice from "@/components/Notice";
import ListCard from "@/components/ListCard";
import GroupDialog from "@/components/dialog/GroupDialog";

export default {
  name: 'FAQView',
  components: {GroupDialog, ListCard, Notice, ConfirmTextDialog, GenericDialog, PostDialog, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    groups: [],
    channels: [],
    locales: ['EN', 'DE'],
    locale: 'EN',
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post OR a channel
    dialogDeletePost: false,
    dialogChannel: false,
    dialogDeleteChannel: false,
    selectedPost: {},
    selectedChannel: {},
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.groups = await getPostsGrouped({ type: 'FAQ' });
      this.channels = this.groups.map((g) => g.channel);
      this.fetching = false;
    },
    showCreateChannel: function() {
      this.$refs.channelDialog.reset();
      this.dialogUpdating = false;
      this.dialogChannel = true;
    },
    showUpdateChannel: function(channel) {
      this.$refs.channelDialog.load(channel.name.en, channel.name.de);
      this.selectedChannel = channel;
      this.dialogUpdating = true;
      this.dialogChannel = true;
    },
    showDeleteChannel: function(channel) {
      this.$refs.deleteChannelDialog.reset();
      this.selectedChannel = channel;
      this.dialogDeleteChannel = true;
    },
    showCreatePost: function(channel) {
      this.$refs.postDialog.reset(channel, this.locale);
      this.dialogPost = true;
      this.dialogUpdating = false;
    },
    showUpdatePost: function(post) {
      // apply post
      this.selectedPost = post;
      this.$refs.postDialog.reset(post.channel, this.locale);
      this.$refs.postDialog.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showDeletePost: function(post) {
      this.selectedPost = post;
      this.dialogDeletePost = true;
    },
    submitChannel: async function(channel) {
      if (this.dialogUpdating)
        await this.updateChannel(channel);
      else
        await this.createChannel(channel);
    },
    createChannel: async function(channel) {
      try {
        this.loading = true;
        await createChannel({ type: 'FAQ', ...channel });
        this.dialogChannel = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateChannel: async function(channel) {
      try {
        this.loading = true;
        await renameChannel({ id: this.selectedChannel.id, ...channel });
        this.dialogChannel = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteChannel: async function() {
      try {
        this.loading = true;
        await deleteChannel({ id: this.selectedChannel.id });
        this.dialogDeleteChannel = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
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
    deletePost: async function() {
      try {
        this.loading = true;
        await deletePost({ id: this.selectedPost.id });
        this.dialogDeletePost = false;
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
    confirmText: function() {
      return this.selectedChannel.name ? this.selectedChannel.name.de : '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>