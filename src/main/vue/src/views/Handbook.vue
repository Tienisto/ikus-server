<template>
  <MainContainer title="Handbook" icon="mdi-file-document">

    <template v-slot:intro>
      Hier können das Handbuch verwalten.
      <br>
      Nach dem Upload versucht das System, ein Inhaltsverzeichnis zu erstellen, den Sie im Nachhinein bearbeiten können.
    </template>
    <v-row>
      <v-col cols="6">
        <HandbookCard locale="EN" title="englisch" :bookmarks="bookmarksEn" @update="fetchData"/>
      </v-col>
      <v-col cols="6">
        <HandbookCard locale="DE" title="deutsch" :bookmarks="bookmarksDe" @update="fetchData" />
      </v-col>
    </v-row>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";
import HandbookCard from "@/components/cards/HandbookCard";
import {getHandbookBookmarks} from "@/api";
import {showSnackbar} from "@/utils";

export default {
  name: 'HandbookView',
  components: {HandbookCard, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    bookmarksEn: [],
    bookmarksDe: []
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      try {
        const response = await getHandbookBookmarks();
        this.bookmarksEn = response.EN;
        this.bookmarksDe = response.DE;
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.fetching = false;
      }
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>