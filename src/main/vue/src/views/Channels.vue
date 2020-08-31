<template>
  <MainContainer title="Kanäle" icon="mdi-filter">

    <div class="subtitle-1">
      Alle Posts und Events sind einem Kanal zugeordnet.
      <br>
      Hier können Sie diese Kanäle verwalten.
    </div>

    <v-tabs class="mt-10" vertical>
      <v-tab @change="type = 'POST'">
        <v-icon left>mdi-newspaper</v-icon>
        Neuigkeiten
      </v-tab>
      <v-tab @change="type = 'EVENT'">
        <v-icon left>mdi-calendar</v-icon>
        Kalender
      </v-tab>

      <v-tab-item>
        <ChannelTabItem :channels="channels.post" @create="dialogCreate = true" @update="openRenameChannel" @delete="openDeleteChannel" />
      </v-tab-item>

      <v-tab-item>
        <ChannelTabItem :channels="channels.event" @create="dialogCreate = true" @update="openRenameChannel" @delete="openDeleteChannel" />
      </v-tab-item>
    </v-tabs>

    <v-dialog v-model="dialogCreate" width="500">
      <v-card>
        <v-card-title class="headline">
          Neuer Kanal
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-col cols="6">
              <v-text-field v-model="nameEn" label="Name (englisch)" :disabled="loading"></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading"></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="createChannel" color="primary" :loading="loading">
            <v-icon left>mdi-plus</v-icon>
            Erstellen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogRename" width="500">
      <v-card>
        <v-card-title class="headline">
          Kanal umbenennen
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-col cols="6">
              <v-text-field v-model="nameEn" label="Name (englisch)" :disabled="loading"></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading"></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="renameChannel" color="primary" :loading="loading">
            <v-icon left>mdi-pencil</v-icon>
            Umbenennen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDelete" width="500">
      <v-card>
        <v-card-title class="headline">
          Kanal löschen
        </v-card-title>

        <v-card-text>
          Möchten Sie wirklich den Kanal {{ nameDe }} löschen?
          <br>
          Bitte beachten Sie, dass <b>alle</b> Beiträge / Events gelöscht werden, die dem Kanal zugeordnet sind.
          <br>
          Schreiben Sie in dem Feld "{{ nameDe }}", um diesen Kanal zu löschen.
          <br><br>
          <v-text-field v-model="nameCheck" label="Eingabe" :disabled="loading"></v-text-field>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="deleteChannel" color="primary" :loading="loading">
            <v-icon left>mdi-delete</v-icon>
            Löschen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </MainContainer>
</template>

<script>
import {createChannel, deleteChannel, getChannels, renameChannel} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import ChannelTabItem from "@/components/ChannelTabItem";
import {showSnackbar} from "@/utils";

export default {
  name: 'ChannelsView',
  components: {ChannelTabItem, MainContainer},
  data: () => ({
    loading: false,
    channels: {
      post: [],
      event: []
    },
    selectedChannel: {},
    type: 'POST',
    dialogCreate: false,
    dialogRename: false,
    dialogDelete: false,
    nameEn: '',
    nameDe: '',
    nameCheck: ''
  }),
  methods: {
    fetchData: async function() {
      this.channels = (await getChannels()).data;
    },
    resetAndCloseAll: function() {
      this.nameEn = '';
      this.nameDe = '';
      this.nameCheck = '';
      this.dialogCreate = false;
      this.dialogRename = false;
      this.dialogDelete = false;
    },
    openRenameChannel: function(channel) {
      this.nameEn = channel.name.en;
      this.nameDe = channel.name.de;
      this.selectedChannel = channel;
      this.dialogRename = true;
    },
    openDeleteChannel: function(channel) {
      this.nameDe = channel.name.de;
      this.selectedChannel = channel;
      this.dialogDelete = true;
    },
    createChannel: async function() {
      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      try {
        this.loading = true;
        await createChannel({ type: this.type, name: { en: this.nameEn, de: this.nameDe } });
        await this.fetchData();
        this.resetAndCloseAll();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    renameChannel: async function() {
      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      try {
        this.loading = true;
        await renameChannel({ id: this.selectedChannel.id, name: { en: this.nameEn, de: this.nameDe } });
        await this.fetchData();
        this.resetAndCloseAll();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteChannel: async function() {

      if (this.nameDe !== this.nameCheck) {
        showSnackbar('Name stimmt nicht überein');
        return;
      }

      try {
        this.loading = true;
        await deleteChannel({ id: this.selectedChannel.id });
        await this.fetchData();
        this.resetAndCloseAll();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>