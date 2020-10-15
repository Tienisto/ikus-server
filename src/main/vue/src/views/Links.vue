<template>
  <MainContainer title="Links" icon="mdi-web">

    <template v-slot:intro>
      Hier können Sie die Links verwalten.
      <br>
      Aufgrund der Übersichtlichkeit, muss jeder Link einer Gruppe zugeordnet werden.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" />
      <br>
      <v-btn @click="showCreateGroup" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
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
        <v-btn @click="moveUpLink(item)" :disabled="loading" icon small>
          <v-icon>mdi-arrow-up</v-icon>
        </v-btn>
        <v-btn @click="moveDownLink(item)" :disabled="loading" class="ml-2" icon small>
          <v-icon>mdi-arrow-down</v-icon>
        </v-btn>
        <v-btn @click="showUpdateLink(item)" :disabled="loading" class="ml-2" icon small>
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
        <v-btn @click="showDeleteLink(item)" :disabled="loading" class="ml-2" icon small>
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </template>
    </ListCard>

    <!-- DIALOGS -->

    <LinkDialog ref="linkDialog" v-model="dialogLink" :channels="channels"
                :updating="dialogUpdating" :loading="loading"
                @submit="submitLink"/>

    <GenericDialog v-model="dialogDeleteLink" title="Link löschen">
      <template v-slot:content>
        Folgender Link wird gelöscht:
        <br>
        <b>{{ selectedLink.info ? selectedLink.info.en : '' }}</b>
        <br><br>
        Möchten Sie wirklich diesen Link löschen?
        <br>
        Dieser Vorgang ist nicht widerrufbar.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDeleteLink = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deleteLink" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

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

<script>
import {
  createLink, createChannel,
  deleteLink, deleteChannel,
  getLinksGrouped,
  updateLink,
  renameChannel,
  moveDownLink, moveUpLink, moveDownChannel, moveUpChannel
} from "@/api";
import {localizedString, showSnackbar} from "@/utils";
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import LoadingIndicator from "@/components/LoadingIndicator";
import Notice from "@/components/Notice";
import ListCard from "@/components/ListCard";
import LinkDialog from "@/components/dialog/LinkDialog";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import GroupDialog from "@/components/dialog/GroupDialog";

export default {
  name: 'LinksView',
  components: {GroupDialog, GenericDialog, ConfirmTextDialog, LinkDialog, ListCard, Notice, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    data: [],
    channels: [],
    locale: 'EN',
    dialogLink: false,
    dialogUpdating: false, // true if dialog is used for updating a link OR a group
    dialogDeleteLink: false,
    dialogGroup: false,
    dialogDeleteGroup: false,
    selectedLink: {},
    selectedGroup: {},
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.data = await getLinksGrouped();
      this.channels = this.data.map((g) => g.channel);
      this.fetching = false;
    },
    showCreateGroup: function() {
      this.$refs.groupDialog.reset();
      this.dialogUpdating = false;
      this.dialogGroup = true;
    },
    showUpdateGroup: function(group) {
      this.$refs.groupDialog.load(group.name.en, group.name.de);
      this.selectedGroup = group;
      this.dialogUpdating = true;
      this.dialogGroup = true;
    },
    showDeleteGroup: function(group) {
      this.$refs.deleteGroupDialog.reset();
      this.selectedGroup = group;
      this.dialogDeleteGroup = true;
    },
    showCreateLink: function(group) {
      this.$refs.linkDialog.reset(group);
      this.dialogLink = true;
      this.dialogUpdating = false;
    },
    showUpdateLink: function(link) {
      // apply link
      this.selectedLink = link;
      this.$refs.linkDialog.reset(link.channel);
      this.$refs.linkDialog.load(link);
      this.dialogUpdating = true;
      this.dialogLink = true;
    },
    showDeleteLink: function(link) {
      this.selectedLink = link;
      this.dialogDeleteLink = true;
    },
    submitGroup: async function(group) {
      if (this.dialogUpdating)
        await this.updateGroup(group);
      else
        await this.createGroup(group);
    },
    createGroup: async function(group) {
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
    updateGroup: async function(group) {
      try {
        this.loading = true;
        await renameChannel({ id: this.selectedGroup.id, ...group });
        this.dialogGroup = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveUpChannel: async function(channel) {
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
    moveDownChannel: async function(channel) {
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
    submitLink: async function(link) {
      if (this.dialogUpdating)
        await this.updateLink(link);
      else
        await this.createLink(link);
    },
    createLink: async function(link) {
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
    updateLink: async function(link) {
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
    moveUpLink: async function(link) {
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
    moveDownLink: async function(link) {
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
      return (obj) => localizedString(obj, this.locale);
    },
    confirmText: function() {
      return this.selectedGroup.name ? this.selectedGroup.name.de : '';
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>