<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" title="PDF-Export">
    <template v-slot:content>
      <p>Zu exportierende Daten auswählen:</p>
      <v-checkbox v-model="matriculationNumber" label="Matrikel-Nr." hide-details />
      <v-checkbox v-model="firstName" label="Vorname" hide-details />
      <v-checkbox v-model="lastName" label="Nachname" hide-details />
      <v-checkbox v-model="email" label="E-Mail" hide-details />
      <v-checkbox v-model="address" label="Adresse" hide-details />
      <v-checkbox v-model="country" label="Land" hide-details />
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text>
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary">
        <v-icon left>mdi-export-variant</v-icon>
        Exportieren
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";
import {registrationFields} from "@/model";

export default {
  name: 'EventRegistrationExportDialog',
  components: {GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    }
  },
  data: () => ({
    matriculationNumber: false,
    firstName: true,
    lastName: true,
    email: false,
    address: false,
    country: false
  }),
  methods: {
    submit: function() {
      const fields = [];
      if (this.matriculationNumber)
        fields.push(registrationFields.MATRICULATION_NUMBER);
      if (this.firstName)
        fields.push(registrationFields.FIRST_NAME);
      if (this.lastName)
        fields.push(registrationFields.LAST_NAME);
      if (this.email)
        fields.push(registrationFields.EMAIL);
      if (this.address)
        fields.push(registrationFields.ADDRESS);
      if (this.country)
        fields.push(registrationFields.COUNTRY);
      this.$emit('submit', fields);
    }
  }
}
</script>