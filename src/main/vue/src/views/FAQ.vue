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

    <v-row v-if="!fetching">
      <v-col cols="6" v-for="g in groups" :key="g.channel.id" class="pt-0 pb-6">
        <v-card>
          <v-card-title>
            <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
              <span>
                {{ localized(g.channel.name) }}
                <v-btn @click="showUpdateChannel(g.channel)" class="black--text ml-2" icon small>
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
                <v-btn @click="showDeleteChannel(g.channel)" class="black--text ml-2" icon small>
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </span>
              <v-btn @click="showCreatePost(g.channel)" color="primary" small>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </v-card-title>
          <v-card-text class="mt-2">
            <v-card outlined>
              <v-card-text class="pa-0">
                <div style="height: 250px; overflow-y: scroll" >

                  <div v-if="!fetching && g.posts.length === 0" class="pl-2 pt-2">
                    <Notice title="Noch keine Fragen." />
                  </div>

                  <div v-for="p in g.posts" :key="p.id" @mouseover="mouseOverPost = p.id" @mouseleave="mouseOverPost = -1">
                    <div class="ml-2 mt-2 mb-2" style="display: flex; align-items: center">
                      <div style="flex: 1">
                        <span class="">{{ localized(p.title) }}</span>
                      </div>
                      <div style="display: flex" class="pl-1 pr-1" :style="mouseOverPost === p.id ? {} : { 'visibility': 'hidden' }">
                        <v-btn @click="showUpdatePost(p)" icon small>
                          <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                        <v-btn @click="showDeletePost(p)" class="ml-2" icon small>
                          <v-icon>mdi-delete</v-icon>
                        </v-btn>
                      </div>
                    </div>
                    <v-divider />
                  </div>
                </div>
              </v-card-text>
            </v-card>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

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

    <ChannelDialog ref="channelDialog" v-model="dialogChannel" channel-type="FAQ" :updating="dialogUpdating" :loading="loading"
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
import MainContainer from "@/components/layout/MainContainer";
import {createChannel, createPost, deleteChannel, deletePost, getPostsGrouped, renameChannel, updatePost} from "@/api";
import LocaleSelector from "@/components/LocaleSelector";
import {localizedString, showSnackbar} from "@/utils";
import moment from "moment";
import LoadingIndicator from "@/components/LoadingIndicator";
import PostDialog from "@/components/dialog/PostDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import ChannelDialog from "@/components/dialog/ChannelDialog";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import Notice from "@/components/Notice";

export default {
  name: 'FAQView',
  components: {
    Notice,
    ConfirmTextDialog,
    ChannelDialog, GenericDialog, PostDialog, LoadingIndicator, LocaleSelector, MainContainer},
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
    mouseOverPost: -1
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
    validateChannel: function(channel) {
      if (!channel.name.en || !channel.name.de) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return false;
      }
      return true;
    },
    submitChannel: async function(channel) {
      if (this.dialogUpdating)
        await this.updateChannel(channel);
      else
        await this.createChannel(channel);
    },
    createChannel: async function(channel) {

      if(!this.validateChannel(channel))
        return;

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

      if(!this.validateChannel(channel))
        return;

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
    validatePost: function(post) {
      if (!post.title.en || !post.title.de) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return false;
      }
      return true;
    },
    submitPost: async function(post) {
      if (this.dialogUpdating)
        await this.updatePost(post);
      else
        await this.createPost(post);
    },
    createPost: async function(post) {

      if(!this.validatePost(post))
        return;

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
      if(!this.validatePost(post))
        return;

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