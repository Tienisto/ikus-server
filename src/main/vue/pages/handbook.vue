<template>
  <MainContainer title="Handbook" icon="mdi-file-document">

    <template v-slot:intro>
      Hier können Sie das Handbuch verwalten.
      <br>
      Das System kann automatisch ein Inhaltsverzeichnis erstellen, den Sie im Nachhinein bearbeiten können.
    </template>

    <v-row>
      <v-col cols="6">
        <HandbookComponent locale="EN" title="englisch" :bookmarks="bookmarksEn" @update="fetchData"/>
      </v-col>
      <v-col cols="6">
        <HandbookComponent locale="DE" title="deutsch" :bookmarks="bookmarksDe" @update="fetchData" />
      </v-col>
    </v-row>

  </MainContainer>
</template>

<script lang="ts">
import {getHandbookBookmarks} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import HandbookComponent from "@/components/HandbookComponent";
import {showSnackbar} from "~/utils/snackbar";
import type {HandbookBookmarkDto} from "~/models";

export default {
  name: 'HandbookView',
  components: {HandbookComponent, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    bookmarksEn: [] as HandbookBookmarkDto[],
    bookmarksDe: [] as HandbookBookmarkDto[],
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
