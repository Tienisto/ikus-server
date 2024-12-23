<template>
  <MainContainer title="Kanäle" icon="mdi-filter">

    <template v-slot:intro>
      Alle Posts und Events sind einem Kanal zugeordnet.
      <br>
      Hier können Sie diese Kanäle verwalten.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-0">Modus</p>

      <v-list class="mt-2 mb-4" style="width: 200px" v-model:selected="type">
        <v-list-item @click="type = 'NEWS'"
                     :class="type == 'NEWS' ? 'text-pink-darken-4 bg-pink-lighten-5' : ''"
                     prepend-icon="mdi-newspaper" title="Neuigkeiten" rounded>
        </v-list-item>

        <v-list-item @click="type = 'CALENDAR'"
                     :class="type == 'CALENDAR' ? 'text-pink-darken-4 bg-pink-lighten-5' : ''"
                     prepend-icon="mdi-calendar" title="Kalender" rounded>
        </v-list-item>
      </v-list>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading" prepend-icon="mdi-plus">
        Neuer Kanal
      </v-btn>
    </template>

    <v-card>
      <v-card-text>

        <v-data-table
            :loading="loading"
            :headers="[
                { title: 'Name (englisch)', key: 'name.en', value: item => item.name.en },
                { title: 'Name (deutsch)', key: 'name.de', value: item => item.name.de },
                { title: 'Aktionen', value: 'actions' }
            ]"
            :items="currChannels"
            :items-per-page="-1"
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

<script lang="ts">
import {createChannel, deleteChannel, getChannels, renameChannel} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog.vue";
import GroupDialog from "@/components/dialog/GroupDialog.vue";
import {showSnackbar} from "@/utils/snackbar";
import type {AllChannelDto, ChannelDto, CreateChannelDto} from "~/models";

export default {
  name: 'ChannelsView',
  components: {GroupDialog, ConfirmTextDialog, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: {
      post: [],
      event: []
    } as AllChannelDto,
    type: 'NEWS',
    mode: 0, // only used for list state
    dialogChannel: false,
    dialogUpdating: false,
    dialogDelete: false,
    selectedChannel: {} as ChannelDto
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.channels = await getChannels({}) as AllChannelDto;
      this.fetching = false;
    },
    showCreateDialog: function() {
      this.$refs.channelDialog.reset();
      this.dialogUpdating = false;
      this.dialogChannel = true;
    },
    showRenameDialog: function(channel: ChannelDto) {
      this.$refs.channelDialog.load(channel.name.en, channel.name.de);
      this.selectedChannel = channel;
      this.dialogUpdating = true;
      this.dialogChannel = true;
    },
    showDeleteDialog: function(channel: ChannelDto) {
      this.$refs.deleteDialog.reset();
      this.selectedChannel = channel;
      this.dialogDelete = true;
    },
    createOrRename: async function(channel: CreateChannelDto) {
      if (this.dialogUpdating)
        await this.renameChannel(channel);
      else
        await this.createChannel(channel);
    },
    createChannel: async function(channel: CreateChannelDto) {
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
    renameChannel: async function(channel: CreateChannelDto) {
      try {
        this.loading = true;
        await renameChannel({
          // @ts-ignore
          id: this.selectedChannel.id,
          ...channel,
        });
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
