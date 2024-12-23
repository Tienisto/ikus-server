<template>
  <MainContainer title="Datenschutzerklärung" icon="mdi-server-security">

    <v-card style="display: inline-block">
      <v-card-title>
        Welcome to OVGU
      </v-card-title>
      <v-card-text class="text-body-2">
        <br>
        <p class="font-weight-bold">Verantwortliche/r</p>
        M.A. Anne-Katrin Güldenpfennig
        <br>
        Dezernat Akademisches Auslandsamt (K6)
        <br>
        G18-149, Universitätsplatz 2
        <br>
        39106 Magdeburg
        <br>
        Deutschland
        <br><br>
        E-Mail: anne-katrin.gueldenpfennig@ovgu.de
        <br><br>
        <p class="font-weight-bold">Verarbeitetende Daten</p>
        Die Welcome to OVGU - App sammelt, speichert oder verteilt keinerlei persönliche Informationen.
      </v-card-text>
    </v-card>

    <br><br>
    <ToHomePageButton/>

  </MainContainer>
</template>

<script setup lang="ts">
import MainContainer from "~/components/layout/MainContainer.vue";
import ToHomePageButton from "~/components/button/ToHomePageButton.vue";
</script>
