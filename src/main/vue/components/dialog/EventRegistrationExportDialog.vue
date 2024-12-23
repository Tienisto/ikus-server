<template>
  <GenericDialog v-model="value" title="Export">
    <template v-slot:content>
      <p>Zu exportierende Daten auswählen:</p>
      <v-checkbox v-model="matriculationNumber" label="Matrikel-Nr." hide-details />
      <v-checkbox v-model="firstName" label="Vorname" hide-details />
      <v-checkbox v-model="lastName" label="Nachname" hide-details />
      <v-checkbox v-model="email" label="E-Mail" hide-details />
      <v-checkbox v-model="address" label="Adresse" hide-details />
      <v-checkbox v-model="country" label="Land" hide-details />
      <v-checkbox v-model="dateOfBirth" label="Geburtsdatum" hide-details />
    </template>

    <template v-slot:actions>
      <v-btn @click="value = false">
        Abbrechen
      </v-btn>
      <v-btn @click="submit('PDF')" color="primary" prepend-icon="mdi-export-variant" variant="elevated">
        PDF
      </v-btn>
      <v-btn @click="submit('WORD')" color="primary" prepend-icon="mdi-export-variant" variant="elevated">
        Word
      </v-btn>
      <v-btn @click="submit('EXCEL')" color="primary" prepend-icon="mdi-export-variant" variant="elevated">
        EXCEL
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script lang="ts">
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import {type EventDto, RegistrationField} from "@/models";

export default {
  name: 'EventRegistrationExportDialog',
  components: {GenericDialog},
  props: {
    modelValue: {
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
    country: false,
    dateOfBirth: false,
  }),
  methods: {
    load: function(event: EventDto) {
      // apply
      this.matriculationNumber = event.registrationFields.some((f) => f === 'MATRICULATION_NUMBER');
      this.firstName = event.registrationFields.some((f) => f === 'FIRST_NAME');
      this.lastName = event.registrationFields.some((f) => f === 'LAST_NAME');
      this.email = event.registrationFields.some((f) => f === 'EMAIL');
      this.address = event.registrationFields.some((f) => f === 'ADDRESS');
      this.country = event.registrationFields.some((f) => f === 'COUNTRY');
      this.dateOfBirth = event.registrationFields.some((f) => f === 'DATE_OF_BIRTH');
    },
    submit: function(type: string) {
      const fields = [];
      if (this.matriculationNumber)
        fields.push(RegistrationField.MATRICULATION_NUMBER);
      if (this.firstName)
        fields.push(RegistrationField.FIRST_NAME);
      if (this.lastName)
        fields.push(RegistrationField.LAST_NAME);
      if (this.email)
        fields.push(RegistrationField.EMAIL);
      if (this.address)
        fields.push(RegistrationField.ADDRESS);
      if (this.country)
        fields.push(RegistrationField.COUNTRY);
      if (this.dateOfBirth)
        fields.push(RegistrationField.DATE_OF_BIRTH);
      this.$emit('submit', fields, type);
    }
  },
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value: boolean) {
        this.$emit('update:modelValue', value)
      }
    },
  }
}
</script>