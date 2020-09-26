<template>
  <MainContainer title="Links" icon="mdi-web">

    <template v-slot:intro>
      Hier können Sie die Links verwalten.
      <br>
      Aufgrund der Übersichtlichkeit, muss jeder Link einer Gruppe zugeordnet werden.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" :locales="locales"/>
      <br>
      <v-btn @click="showCreateGroup" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neue Gruppe
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching" />

    <Notice v-if="!fetching && groups.length === 0"
            title="Es existieren noch keine Gruppen" info="Sie können rechts eine neue Gruppe erstellen" />

    <div v-if="!fetching">
      <ListCard v-for="g in data" :key="g.group.id"
                :title="localized(g.group.name)" :items="g.links" :empty-notice="!fetching && g.links.length === 0" empty-notice-text="Noch keine Links"
                @edit="showUpdateGroup(g.group)" @delete="showDeleteGroup(g.group)" @create="showCreateLink(g.group)"
                class="mb-6">

        <template v-slot:item="{ item }">
          <span class="">
            <b>{{ localized(item.info) }}</b>
            <br>
            {{ localized(item.url) }}
          </span>
        </template>

        <template v-slot:actions="{ item }">
          <v-btn @click="showUpdateLink(item)" icon small>
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn @click="showDeleteLink(item)" class="ml-2" icon small>
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
      </ListCard>
    </div>

    <!-- DIALOGS -->

    <LinkDialog ref="linkDialog" v-model="dialogLink" :groups="groups"
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

    <ChannelDialog ref="groupDialog" v-model="dialogGroup" channel-type="FAQ" :updating="dialogUpdating" :loading="loading"
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
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import {
  createLink, createLinkGroup,
  deleteLink, deleteLinkGroup,
  getLinksGrouped,
  updateLink, updateLinkGroup,
} from "@/api";
import {localizedString, showSnackbar} from "@/utils";
import LoadingIndicator from "@/components/LoadingIndicator";
import Notice from "@/components/Notice";
import ListCard from "@/components/ListCard";
import LinkDialog from "@/components/dialog/LinkDialog";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import ChannelDialog from "@/components/dialog/ChannelDialog";
import GenericDialog from "@/components/dialog/GenericDialog";

export default {
  name: 'LinksView',
  components: {
    GenericDialog,
    ChannelDialog,
    ConfirmTextDialog, LinkDialog, ListCard, Notice, LoadingIndicator, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    data: [],
    groups: [],
    locales: ['EN', 'DE'],
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
      this.groups = this.data.map((g) => g.group);
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
      this.$refs.linkDialog.reset(link.group);
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
        await createLinkGroup(group);
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
        await updateLinkGroup({ id: this.selectedGroup.id, ...group });
        this.dialogGroup = false;
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
        await deleteLinkGroup({ id: this.selectedGroup.id });
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