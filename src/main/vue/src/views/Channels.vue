<template>
  <MainContainer title="Kanäle" icon="mdi-filter">

    <div class="subtitle-1">
      Alle Posts und Events sind einem Kanal zugeordnet.
      <br>
      Hier können Sie diese Kanäle verwalten.
    </div>

    <v-tabs class="mt-10" vertical>
      <v-tab @change="updateType('POST')">
        <v-icon left>mdi-newspaper</v-icon>
        Neuigkeiten
      </v-tab>
      <v-tab @change="updateType('EVENT')">
        <v-icon left>mdi-calendar</v-icon>
        Kalender
      </v-tab>

      <v-tab-item>
        <ChannelTabItem :channels="channels.post" @create="dialogCreate = true" />
      </v-tab-item>

      <v-tab-item>
        <ChannelTabItem :channels="channels.event" @create="dialogCreate = true" />
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
              <v-text-field v-model="nameEn" label="Name (englisch)"></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="nameDe" label="Name (deutsch)"></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text>
            Abbrechen
          </v-btn>
          <v-btn @click="createChannel" color="primary">
            <v-icon left>mdi-plus</v-icon>
            Erstellen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </MainContainer>
</template>

<script>
import {createChannel, getChannels} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import ChannelTabItem from "@/components/ChannelTabItem";
import {showSnackbar} from "@/utils";

export default {
  name: 'ChannelsView',
  components: {ChannelTabItem, MainContainer},
  data: () => ({
    channels: {
      post: [],
      event: []
    },
    type: 'POST',
    dialogCreate: false,
    nameEn: '',
    nameDe: ''
  }),
  methods: {
    fetchData: async function() {
      this.channels = (await getChannels()).data;
    },
    resetAndCloseAll: function() {
      this.nameEn = '';
      this.nameDe = '';
      this.dialogCreate = false;
    },
    updateType: async function(newType) {
      this.type = newType;
      await this.fetchData();
    },
    createChannel: async function() {
      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      try {
        await createChannel({ type: this.type, name: { en: this.nameEn, de: this.nameDe } });
        this.resetAndCloseAll();
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      }
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>