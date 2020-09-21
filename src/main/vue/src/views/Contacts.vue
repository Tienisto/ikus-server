<template>
  <MainContainer title="Kontakte" icon="mdi-card-account-mail">

    <template v-slot:intro>
      Hier können Sie die Kontakte verwalten.
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'ContactView',
  components: {MainContainer},
}
</script>