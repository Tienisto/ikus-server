<template>
  <MainContainer title="Links" icon="mdi-web">

    <template v-slot:intro>
      Hier können Sie die Links verwalten.
      <br>
      Aufgrund der Übersichtlichkeit, muss jeder Link einer Gruppe zugeordnet werden.
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'LinksView',
  components: {MainContainer},
}
</script>