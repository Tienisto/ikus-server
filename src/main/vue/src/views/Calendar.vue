<template>
  <MainContainer title="Kalender" icon="mdi-calendar">

    <template v-slot:intro>
      Hier können Sie den Kalender verwalten.
      <br>
      Alle Events sind einem Kanal zugeordnet.
    </template>

  </MainContainer>
</template>

<script>
import MainContainer from "@/components/layout/MainContainer";

export default {
  name: 'CalendarView',
  components: {MainContainer},
}
</script>