<template>
  <MainContainer title="FAQ" icon="mdi-help-circle">

    <template v-slot:intro>
      Hier können Sie die häufig gestellte Fragen verwalten.
      <br>
      Zu jeder Frage können Sie einen Beitrag verfassen (inkl. Bilder).
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'FAQView',
  components: {MainContainer},
}
</script>