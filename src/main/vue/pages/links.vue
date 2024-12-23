<template>
  <MainContainer title="Links" icon="mdi-web">

    <template v-slot:intro>
      Hier können Sie die Links verwalten.
      <br>
      Aufgrund der Übersichtlichkeit, muss jeder Link einer Gruppe zugeordnet werden.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>
      <LocaleSelector v-model="locale" />
      <br>
      <v-btn @click="showCreateGroup" color="primary" block :disabled="loading" prepend-icon="mdi-plus">
        Neue Gruppe
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && channels.length === 0" />

    <Notice v-if="!fetching && channels.length === 0"
            title="Es existieren noch keine Gruppen" info="Sie können rechts eine neue Gruppe erstellen" />

    <ListCard v-for="g in data" :key="g.channel.id"
              :loading="loading"
              :title="localized(g.channel.name)" :items="g.links" :empty-notice="!fetching && g.links.length === 0" empty-notice-text="Noch keine Links"
              @move-up="moveUpChannel(g.channel)" @move-down="moveDownChannel(g.channel)"
              @edit="showUpdateGroup(g.channel)" @delete="showDeleteGroup(g.channel)" @create="showCreateLink(g.channel)"
              class="mb-6">

      <template v-slot:item="{ item }">
        <span class="">
          <b>{{ localized(item.info) }}</b>
          <br>
          {{ localized(item.url) }}
        </span>
      </template>

      <template v-slot:actions="{ item }">
        <v-btn @click="moveUpLink(item)" icon="mdi-arrow-up"
               :disabled="loading" variant="text" size="small" />
        <v-btn @click="moveDownLink(item)" icon="mdi-arrow-down"
               :disabled="loading" variant="text" size="small" />
        <v-btn @click="showUpdateLink(item)" icon="mdi-pencil"
               :disabled="loading" variant="text" size="small" />
        <v-btn @click="showDeleteLink(item)" icon="mdi-delete"
               :disabled="loading" variant="text" size="small" />
      </template>
    </ListCard>

    <!-- DIALOGS -->

    <LinkDialog ref="linkDialog" v-model="dialogLink" :channels="channels"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitLink"/>

    <GenericDeleteDialog v-model="dialogDeleteLink" dialog-title="Link löschen"
                         @delete="deleteLink" :loading="loading">
      Folgender Link wird gelöscht:
      <br>
      <b>{{ selectedLink.info ? selectedLink.info.en : '' }}</b>
      <br><br>
      Möchten Sie wirklich diesen Link löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>

    <GroupDialog ref="groupDialog" v-model="dialogGroup" :dialog-title="dialogUpdating ? 'Gruppe umbenennen' : 'Neue Gruppe'" :updating="dialogUpdating" :loading="loading"
                 @submit="submitGroup"/>

    <ConfirmTextDialog ref="deleteGroupDialog" v-model="dialogDeleteGroup" :confirm-text="confirmText" :loading="loading" title="Gruppe löschen"
                       @submit="deleteGroup">
      Möchten Sie wirklich die Gruppe {{ confirmText }} löschen?
      <br>
      Bitte beachten Sie, dass <b>alle</b> Links gelöscht werden, die der Gruppe zugeordnet sind.
      <br>
      Schreiben Sie in dem Feld "{{ confirmText }}", um diese Gruppe zu löschen.
    </ConfirmTextDialog>

  </MainContainer>
</template>

<script lang="ts">
import {
  createLink, createChannel,
  deleteLink, deleteChannel,
  getLinksGrouped,
  updateLink,
  renameChannel,
  moveDownLink, moveUpLink, moveDownChannel, moveUpChannel
} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import Notice from "@/components/Notice.vue";
import ListCard from "@/components/ListCard.vue";
import LinkDialog from "@/components/dialog/LinkDialog.vue";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import GroupDialog from "@/components/dialog/GroupDialog.vue";
import type {ChannelDto, CreateLinkDto, LinkDto, LinkGroupDto, MultiLocaleString} from "~/models";
import {showSnackbar} from "~/utils/snackbar";
import {localizedString} from "~/utils/localization";
import {useTemplateRef} from "vue";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";

export default {
  name: 'LinksView',
  components: {
    GenericDeleteDialog,
    GroupDialog, GenericDialog, ConfirmTextDialog, LinkDialog, ListCard, Notice, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    data: [] as LinkGroupDto[],
    channels: [] as ChannelDto[],
    locale: 'EN',
    dialogLink: false,
    dialogUpdating: false, // true if dialog is used for updating a link OR a group
    dialogDeleteLink: false,
    dialogGroup: false,
    dialogDeleteGroup: false,
    selectedLink: {} as LinkDto,
    selectedGroup: {} as ChannelDto,
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.data = await getLinksGrouped();
      this.channels = this.data.map((g) => g.channel);
      this.fetching = false;
    },
    showCreateGroup: function() {
      this.groupDialog!.reset();
      this.dialogUpdating = false;
      this.dialogGroup = true;
    },
    showUpdateGroup: function(group: ChannelDto) {
      this.groupDialog!.load(group.name.en, group.name.de);
      this.selectedGroup = group;
      this.dialogUpdating = true;
      this.dialogGroup = true;
    },
    showDeleteGroup: function(group: ChannelDto) {
      this.deleteGroupDialog!.reset();
      this.selectedGroup = group;
      this.dialogDeleteGroup = true;
    },
    showCreateLink: function(group: ChannelDto) {
      this.linkDialog!.reset(group);
      this.dialogLink = true;
      this.dialogUpdating = false;
    },
    showUpdateLink: function(link: LinkDto) {
      // apply link
      this.selectedLink = link;
      this.linkDialog!.reset(link.channel);
      this.linkDialog!.load(link);
      this.dialogUpdating = true;
      this.dialogLink = true;
    },
    showDeleteLink: function(link: LinkDto) {
      this.selectedLink = link;
      this.dialogDeleteLink = true;
    },
    submitGroup: async function(group: ChannelDto) {
      if (this.dialogUpdating)
        await this.updateGroup(group);
      else
        await this.createGroup(group);
    },
    createGroup: async function(group: ChannelDto) {
      try {
        this.loading = true;
        await createChannel({ type: 'LINK', ...group });
        this.dialogGroup = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateGroup: async function(group: ChannelDto) {
      try {
        this.loading = true;
        await renameChannel({
          // @ts-ignore: overwrite id
          id: this.selectedGroup.id,
          ...group,
        });
        this.dialogGroup = false;
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
    deleteGroup: async function() {
      try {
        this.loading = true;
        await deleteChannel({ id: this.selectedGroup.id });
        this.dialogDeleteGroup = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    submitLink: async function(link: CreateLinkDto) {
      if (this.dialogUpdating)
        await this.updateLink(link);
      else
        await this.createLink(link);
    },
    createLink: async function(link: CreateLinkDto) {
      try {
        this.loading = true;
        await createLink(link);
        this.dialogLink = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateLink: async function(link: CreateLinkDto) {
      try {
        this.loading = true;
        await updateLink({
          id: this.selectedLink.id,
          ...link
        });
        this.dialogLink = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveUpLink: async function(link: LinkDto) {
      try {
        this.loading = true;
        await moveUpLink({id: link.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownLink: async function(link: LinkDto) {
      try {
        this.loading = true;
        await moveDownLink({id: link.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteLink: async function() {
      try {
        this.loading = true;
        await deleteLink({ id: this.selectedLink.id });
        this.dialogDeleteLink = false;
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
      return this.selectedGroup.name ? this.selectedGroup.name.de : '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  },
  setup: function() {
    const groupDialog = useTemplateRef<InstanceType<typeof GroupDialog>>('groupDialog');
    const linkDialog = useTemplateRef<InstanceType<typeof LinkDialog>>('linkDialog');
    const deleteGroupDialog = useTemplateRef<InstanceType<typeof ConfirmTextDialog>>('deleteGroupDialog');

    return {groupDialog, linkDialog, deleteGroupDialog};
  },
}
</script>
