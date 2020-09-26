<template>
  <MainContainer title="Kanäle" icon="mdi-filter">

    <template v-slot:intro>
      Alle Posts und Events sind einem Kanal zugeordnet.
      <br>
      Hier können Sie diese Kanäle verwalten.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-0">Modus</p>

      <v-list class="mt-2 mb-4" style="width: 200px">
        <v-list-item-group v-model="mode" color="pink darken-4" mandatory>
          <v-list-item @click="type = 'NEWS'">
            <v-list-item-icon>
              <v-icon>mdi-newspaper</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                Neuigkeiten
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-list-item @click="type = 'CALENDAR'">
            <v-list-item-icon>
              <v-icon>mdi-calendar</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                Kalender
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list-item-group>
      </v-list>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neuer Kanal
      </v-btn>
    </template>

    <v-card>
      <v-card-text>

        <v-data-table
            :loading="loading"
            :headers="[
                { text: 'Name (englisch)', value: 'name.en' },
                { text: 'Name (deutsch)', value: 'name.de' },
                { text: 'Aktionen', value: 'actions' }
            ]"
            :items="currChannels"
            loading-text="Lade Daten..."
            no-data-text="Keine Kanäle vorhanden"
            hide-default-footer
        >

          <template v-slot:item.actions="props">
            <v-btn @click="showRenameDialog(props.item)" elevation="2" color="primary">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>

            <v-btn @click="showDeleteDialog(props.item)" class="ml-4" elevation="2" color="primary">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <GroupDialog ref="channelDialog" v-model="dialogChannel" :dialog-title="dialogUpdating ? 'Kanal umbenennen' : 'Neuer Kanal'" :updating="dialogUpdating" :loading="loading"
                  @submit="createOrRename"/>

    <ConfirmTextDialog ref="deleteDialog" v-model="dialogDelete" :confirm-text="confirmText" :loading="loading" title="Kanal löschen"
                        @submit="deleteChannel">
      Möchten Sie wirklich den Kanal {{ confirmText }} löschen?
      <br>
      Bitte beachten Sie, dass <b>alle</b> Beiträge / Events gelöscht werden, die dem Kanal zugeordnet sind.
      <br>
      Schreiben Sie in dem Feld "{{ confirmText }}", um diesen Kanal zu löschen.
    </ConfirmTextDialog>

  </MainContainer>
</template>

<script>
import {createChannel, deleteChannel, getChannels, renameChannel} from "@/api";
import {showSnackbar} from "@/utils";
import MainContainer from "@/components/layout/MainContainer";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";
import GroupDialog from "@/components/dialog/GroupDialog";

export default {
  name: 'ChannelsView',
  components: {GroupDialog, ConfirmTextDialog, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: {
      post: [],
      event: []
    },
    type: 'NEWS',
    mode: 0, // only used for list state
    dialogChannel: false,
    dialogUpdating: false,
    dialogDelete: false,
    selectedChannel: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({});
      this.fetching = false;
    },
    showCreateDialog: function() {
      this.$refs.channelDialog.reset();
      this.dialogUpdating = false;
      this.dialogChannel = true;
    },
    showRenameDialog: function(channel) {
      this.$refs.channelDialog.load(channel.name.en, channel.name.de);
      this.selectedChannel = channel;
      this.dialogUpdating = true;
      this.dialogChannel = true;
    },
    showDeleteDialog: function(channel) {
      this.$refs.deleteDialog.reset();
      this.selectedChannel = channel;
      this.dialogDelete = true;
    },
    createOrRename: async function(channel) {
      if (this.dialogUpdating)
        await this.renameChannel(channel);
      else
        await this.createChannel(channel);
    },
    createChannel: async function(channel) {
      try {
        this.loading = true;
        await createChannel({ type: this.type, ...channel });
        this.dialogChannel = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    renameChannel: async function(channel) {
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
        this.dialogDelete = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    }
  },
  computed: {
    confirmText: function() {
      return this.selectedChannel.name ? this.selectedChannel.name.de : '';
    },
    currChannels: function() {
      if (this.type === 'NEWS')
        return this.channels.post;
      else
        return this.channels.event;
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>