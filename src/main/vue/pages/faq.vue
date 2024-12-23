<template>
  <MainContainer title="FAQ" icon="mdi-help-circle">

    <template v-slot:intro>
      Hier können Sie häufig gestellte Fragen verwalten.
      <br>
      Zu jeder Frage können Sie einen Beitrag verfassen (inkl. Bilder).
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>
      <LocaleSelector v-model="locale" />
      <br>
      <v-btn @click="showCreateChannel" color="primary" block :disabled="loading" prepend-icon="mdi-plus">
        Neue Gruppe
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && groups.length === 0" />

    <Notice v-if="!fetching && groups.length === 0"
            title="Es existieren noch keine Gruppen" info="Sie können rechts eine neue Gruppe erstellen" />

    <ListCard v-for="g in groups" :key="g.channel.id"
              :loading="loading"
              :title="localized(g.channel.name)" :items="g.posts" :empty-notice="!fetching && g.posts.length === 0" empty-notice-text="Noch keine Fragen."
              @move-up="moveUpChannel(g.channel)" @move-down="moveDownChannel(g.channel)"
              @edit="showUpdateChannel(g.channel)" @delete="showDeleteChannel(g.channel)" @create="showCreatePost(g.channel)"
              class="mb-6">

      <template v-slot:item="{ item }">
        <span class="">{{ localized(item.title) }}</span>
      </template>

      <template v-slot:actions="{ item }">
        <v-btn @click="moveUpPost(item)" icon="mdi-arrow-up"
               :disabled="loading" variant="text" size="small"/>
        <v-btn @click="moveDownPost(item)" icon="mdi-arrow-down"
               :disabled="loading" variant="text" size="small" />
        <v-btn @click="showUpdatePost(item)" icon="mdi-pencil"
               :disabled="loading" variant="text" size="small" />
        <v-btn @click="showDeletePost(item)" icon="mdi-delete"
               :disabled="loading" variant="text" size="small" />
      </template>
    </ListCard>

    <!-- DIALOGS -->

    <PostDialog ref="post-dialog" v-model="dialogPost" post-type="FAQ" :channels="channels"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitPost"/>

    <GenericDeleteDialog v-model="dialogDeletePost" dialog-title="Frage löschen"
                         @delete="deletePost" :loading="loading">
      Folgende Frage wird gelöscht:
      <br>
      <b>{{ selectedPost.title ? selectedPost.title.en : '' }}</b>
      <br><br>
      Möchten Sie wirklich diese Frage löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>

    <GroupDialog ref="channel-dialog" v-model="dialogChannel" :dialog-title="dialogUpdating ? 'Gruppe umbenennen' : 'Neue Gruppe'" :updating="dialogUpdating" :loading="loading"
                 @submit="submitChannel"/>

    <ConfirmTextDialog ref="delete-channel-dialog" v-model="dialogDeleteChannel" :confirm-text="confirmText" :loading="loading" title="Gruppe löschen"
                       @submit="deleteChannel">
      Möchten Sie wirklich die Gruppe {{ confirmText }} löschen?
      <br>
      Bitte beachten Sie, dass <b>alle</b> Fragen gelöscht werden, die der Gruppe zugeordnet sind.
      <br>
      Schreiben Sie in dem Feld "{{ confirmText }}", um diesen Kanal zu löschen.
    </ConfirmTextDialog>

  </MainContainer>
</template>

<script lang="ts">
import {
  createChannel,
  createPost,
  deleteChannel,
  deletePost,
  getFaq, moveDownChannel, moveDownPost,
  moveUpChannel, moveUpPost,
  renameChannel,
  updatePost
} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import PostDialog from "@/components/dialog/PostDialog.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog.vue";
import Notice from "@/components/Notice.vue";
import ListCard from "@/components/ListCard.vue";
import GroupDialog from "@/components/dialog/GroupDialog.vue";
import {showSnackbar} from "~/utils/snackbar";
import {formatIsoDate, localizedString} from "~/utils/localization";
import type {ChannelDto, CreateChannelDto, CreatePostDto, MultiLocaleString, PostDto, PostGroupDto} from "~/models";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";
import {useTemplateRef} from "vue";

export default {
  name: 'FAQView',
  components: {GenericDeleteDialog, GroupDialog, ListCard, Notice, ConfirmTextDialog, GenericDialog, PostDialog, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    groups: [] as PostGroupDto[],
    channels: [] as ChannelDto[],
    locale: 'EN',
    dialogPost: false,
    dialogUpdating: false, // true if dialog is used for updating a post OR a channel
    dialogDeletePost: false,
    dialogChannel: false,
    dialogDeleteChannel: false,
    selectedPost: {} as PostDto,
    selectedChannel: {} as ChannelDto,
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.groups = await getFaq();
      this.channels = this.groups.map((g) => g.channel);
      this.fetching = false;
    },
    showCreateChannel: function() {
      this.channelDialog!.reset();
      this.dialogUpdating = false;
      this.dialogChannel = true;
    },
    showUpdateChannel: function(channel: ChannelDto) {
      this.channelDialog!.load(channel.name.en, channel.name.de);
      this.selectedChannel = channel;
      this.dialogUpdating = true;
      this.dialogChannel = true;
    },
    showDeleteChannel: function(channel: ChannelDto) {
      this.deleteChannelDialog!.reset();
      this.selectedChannel = channel;
      this.dialogDeleteChannel = true;
    },
    showCreatePost: function(channel: ChannelDto) {
      this.postDialog!.reset(channel, this.locale, formatIsoDate(new Date()));
      this.dialogPost = true;
      this.dialogUpdating = false;
    },
    showUpdatePost: function(post: PostDto) {
      // apply post
      this.selectedPost = post;
      this.postDialog!.reset(post.channel, this.locale, post.date);
      this.postDialog!.load(post);
      this.dialogUpdating = true;
      this.dialogPost = true;
    },
    showDeletePost: function(post: PostDto) {
      this.selectedPost = post;
      this.dialogDeletePost = true;
    },
    submitChannel: async function(channel: CreateChannelDto) {
      if (this.dialogUpdating)
        await this.updateChannel(channel);
      else
        await this.createChannel(channel);
    },
    createChannel: async function(channel: CreateChannelDto) {
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
    updateChannel: async function(channel: CreateChannelDto) {
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
    moveUpChannel: async function(channel: ChannelDto) {
      try {
        this.loading = true;
        await moveUpChannel({id: channel.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownChannel: async function(channel: ChannelDto) {
      try {
        this.loading = true;
        await moveDownChannel({id: channel.id});
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
    moveUpPost: async function(post: PostDto) {
      try {
        this.loading = true;
        await moveUpPost({id: post.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownPost: async function(post: PostDto) {
      try {
        this.loading = true;
        await moveDownPost({id: post.id});
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
      return (obj: MultiLocaleString) => localizedString(obj, this.locale);
    },
    confirmText: function() {
      return this.selectedChannel.name ? this.selectedChannel.name.de : '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  },
  setup() {
    const channelDialog = useTemplateRef<InstanceType<typeof GroupDialog>>('channel-dialog');
    const deleteChannelDialog = useTemplateRef<InstanceType<typeof ConfirmTextDialog>>('delete-channel-dialog');
    const postDialog = useTemplateRef<InstanceType<typeof PostDialog>>('post-dialog');
    return {channelDialog, deleteChannelDialog, postDialog};
  }
}
</script>
