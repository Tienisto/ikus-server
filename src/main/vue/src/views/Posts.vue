<template>
  <MainContainer title="Posts" icon="mdi-pencil">

    <template v-slot:intro>
      Hier können Sie die Neuigkeiten verwalten.
      <br>
      Alle Posts müssen in englisch und deutsch verfasst werden.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <v-select
          label="Kanal" style="width: 250px"
          v-model="channel" @change="updateChannel"
          :items="channels" item-text="name.en" item-value="id"
          return-object
      ></v-select>

      <div style="display: flex; align-items: center; justify-content: space-between">
        <span>Sprache: </span>
        <v-btn-toggle v-model="locale" color="primary" mandatory dense>
          <v-btn :value="l" v-for="l in locales" :key="l" text>
            {{ l }}
          </v-btn>
        </v-btn-toggle>
      </div>

      <br>

      <v-btn color="primary" block>
        <v-icon left>mdi-plus</v-icon>
        Neuer Post
      </v-btn>
    </template>

    <v-card v-for="p in posts" :key="'p-'+p.id" class="mb-4">
      <v-card-text>
        <div style="display: flex; align-items: center">
          <div style="flex: 1">
            <span class="font-weight-bold">{{ title(p.title) }}</span>
            <br>
            <span>{{ timeString(p.date) }}</span>
          </div>
          <div style="display: flex">
            <v-btn  elevation="2" color="primary">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>

            <v-btn elevation="2" color="primary" class="ml-4">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>

  </MainContainer>
</template>

<script>
import moment from "moment"
import {getChannels, getPosts} from "@/api";
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'PostsView',
  components: {MainContainer},
  data: () => ({
    channels: [],
    channel: {},
    posts: [],
    locales: ['EN', 'DE'],
    locale: 'EN'
  }),
  methods: {
    fetchData: async function() {
      this.channels = (await getChannels({ type: 'NEWS' })).data;
      if (!this.channel.id && this.channels.length !== 0) {
        this.channel = this.channels[0];
      }

      if (this.channel) {
        this.posts = (await getPosts({ channelId: this.channel.id })).data;
      }
    },
    updateChannel: async function() {
      await this.fetchData();
    }
  },
  computed: {
    title: function() {
      return (title) => this.locale === 'EN' ? title.en : title.de;
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>