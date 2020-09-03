<template>
  <MainContainer title="Handbook" icon="mdi-file-document">

    <template v-slot:intro>
      Hier können das Handbuch verwalten.
      <br>
      Nach dem Upload versucht das System, ein Inhaltsverzeichnis zu erstellen, den Sie im Nachhinein bearbeiten können.
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'HandbookView',
  components: {MainContainer},
}
</script>