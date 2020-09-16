<template>
  <MainContainer title="FAQ" icon="mdi-help-circle">

    <template v-slot:intro>
      Hier können Sie die häufig gestellte Fragen verwalten.
      <br>
      Zu jeder Frage können Sie einen Beitrag verfassen (inkl. Bilder).
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" :locales="locales"/>
      <br>
      <v-btn color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neue Gruppe
      </v-btn>
    </template>

    <v-row>
      <v-col cols="6" v-for="p in posts" :key="p.channel.id" class="pt-0 pb-6">
        <v-card>
          <v-card-title>
            {{ localized(p.channel.name) }}
          </v-card-title>
        </v-card>
      </v-col>
    </v-row>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";
import {getPostsGrouped} from "@/api";
import LocaleSelector from "@/components/LocaleSelector";

export default {
  name: 'FAQView',
  components: {LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    posts: [],
    locales: ['EN', 'DE'],
    locale: 'EN',
    dialogCreateUpdate: false,
    dialogUpdating: false, // true if dialog is used for updating a post
    dialogDelete: false,
    selectedPost: {},
    postLocale: 'EN',
    postChannel: {},
    titleEn: '',
    titleDe: '',
    contentEn: '',
    contentDe: '',
    files: []
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.posts = await getPostsGrouped({ type: 'FAQ' });
      this.fetching = false;
    },
  },
  computed: {
    localized: function() {
      return (obj) => this.locale === 'EN' ? obj.en : obj.de;
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>