<template>
  <MainContainer title="Kanäle" icon="mdi-filter">

    <template v-slot:intro>
      Alle Posts und Events sind einem Kanal zugeordnet.
      <br>
      Hier können Sie diese Kanäle verwalten.
    </template>

    <v-card>
      <v-card-text>
        <v-tabs vertical>
          <v-tab @change="type = 'NEWS'">
            <v-icon left>mdi-newspaper</v-icon>
            Neuigkeiten
            <v-spacer/>
          </v-tab>
          <v-tab @change="type = 'CALENDAR'">
            <v-icon left>mdi-calendar</v-icon>
            Kalender
            <v-spacer/>
          </v-tab>

          <v-tab-item>
            <ChannelTabItem :channels="channels.post" :loading="fetching" @create="showCreateDialog" @update="showRenameDialog" @delete="showDeleteDialog" />
          </v-tab-item>

          <v-tab-item>
            <ChannelTabItem :channels="channels.event" :loading="fetching" @create="showCreateDialog" @update="showRenameDialog" @delete="showDeleteDialog" />
          </v-tab-item>
        </v-tabs>
      </v-card-text>
    </v-card>

    <ChannelDialog ref="channelDialog" v-model="dialogChannel" :channel-type="type" :updating="dialogUpdating" :loading="loading"
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
import ChannelTabItem from "@/components/ChannelTabItem";
import ChannelDialog from "@/components/dialog/ChannelDialog";
import ConfirmTextDialog from "@/components/dialog/ConfirmTextDialog";

export default {
  name: 'ChannelsView',
  components: {ConfirmTextDialog, ChannelDialog, ChannelTabItem, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    channels: {
      post: [],
      event: []
    },
    type: 'NEWS',
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
      if (!channel.name.en || !channel.name.de) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

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
      if (!channel.name.en || !channel.name.de) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

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
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>