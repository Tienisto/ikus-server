<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" title="Anmeldungsinformation">
    <template v-slot:content>
      <b>Vorname: </b>
      <br>
      {{ data.firstName || '(keine Angabe)' }}
      <br><br>
      <b>Nachname: </b>
      <br>
      {{ data.lastName || '(keine Angabe)' }}
      <br><br>
      <b>Matrikel-Nr.: </b>
      <br>
      {{ data.matriculationNumber || '(keine Angabe)' }}
      <br><br>
      <b>E-Mail: </b>
      <br>
      {{ data.email || '(keine Angabe)' }}
      <br><br>
      <b>Adresse: </b>
      <br>
      {{ data.address || '(keine Angabe)' }}
      <br><br>
      <b>Land: </b>
      <br>
      {{ data.country || '(keine Angabe)' }}
      <br><br>
      <b>Zeitstempel: </b>
      <br>
      {{ timestamp }}
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text>
        Schließen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import moment from "moment"
import GenericDialog from "@/components/dialog/GenericDialog";

export default {
  name: 'EventRegistrationInfoDialog',
  components: {GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    data: {
      type: Object,
      required: true
    }
  },
  computed: {
    timestamp: function() {
      return moment(this.data.timestamp).format('DD.MM.YYYY, HH:mm');
    },
  }
}
</script>