<template>
  <MainContainer title="Statistiken" icon="mdi-finance">

    <template v-slot:intro>
      Beim Start der IKUS-App wird Daten an den Server gesendet, die im Anschluss anonymisiert abgespeichert werden.
      <br>
      Diese Daten sind hilfreich, damit Sie hier konkrete Zahlen zur App-Nutzung einsehen können.
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'StatisticsView',
  components: {MainContainer},
}
</script>