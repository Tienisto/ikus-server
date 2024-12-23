<template>
  <GenericDialog v-model="value" title="Anmeldungseinstellungen">
    <template v-slot:content>
      <v-row class="mt-1">
        <v-col cols="6">
          <v-text-field v-model="slots" label="Plätze" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-text-field v-model="slotsWaiting" label="Warteliste" :disabled="loading" filled hide-details />
        </v-col>
      </v-row>
      <div class="text-h5 mt-10">Pflichtfelder:</div>
      <v-checkbox v-model="matriculationNumber" label="Matrikel-Nr." hide-details />
      <v-checkbox v-model="firstName" label="Vorname" hide-details />
      <v-checkbox v-model="lastName" label="Nachname" hide-details />
      <v-checkbox v-model="email" label="E-Mail" hide-details />
      <v-checkbox v-model="address" label="Adresse" hide-details />
      <v-checkbox v-model="country" label="Land" hide-details />
      <v-checkbox v-model="dateOfBirth" label="Geburtsdatum" hide-details />
    </template>

    <template v-slot:actions>
      <v-btn @click="value = false" color="black" :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading" prepend-icon="mdi-content-save" variant="elevated">
        Speichern
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script lang="ts">
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import {showSnackbar} from "~/utils/snackbar";
import {type EventDto, RegistrationField} from "~/models";

export default {
  name: 'EventRegistrationConfigDialog',
  components: {GenericDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
  },
  data: () => ({
    slots: '20',
    slotsWaiting: '30',
    matriculationNumber: false,
    firstName: true,
    lastName: true,
    email: false,
    address: false,
    country: false,
    dateOfBirth: false,
  }),
  methods: {
    reset: function() {
      // reset input
      this.slots = '20';
      this.slotsWaiting = '30';
      this.matriculationNumber = false;
      this.firstName = true;
      this.lastName = true;
      this.email = false;
      this.address = false;
      this.country = false;
      this.dateOfBirth = false;
    },
    load: function(event: EventDto) {
      // apply
      this.slots = event.registrationSlots.toString();
      this.slotsWaiting = event.registrationSlotsWaiting.toString();
      this.matriculationNumber = event.registrationFields.some((f) => f === 'MATRICULATION_NUMBER');
      this.firstName = event.registrationFields.some((f) => f === 'FIRST_NAME');
      this.lastName = event.registrationFields.some((f) => f === 'LAST_NAME');
      this.email = event.registrationFields.some((f) => f === 'EMAIL');
      this.address = event.registrationFields.some((f) => f === 'ADDRESS');
      this.country = event.registrationFields.some((f) => f === 'COUNTRY');
      this.dateOfBirth = event.registrationFields.some((f) => f === 'DATE_OF_BIRTH');
    },
    submit: async function() {
      const slots = parseInt(this.slots);
      const slotsWaiting = parseInt(this.slotsWaiting);

      if (isNaN(slots) || isNaN(slotsWaiting) || slots <= 0 || slotsWaiting <= 0) {
        showSnackbar('Nur positive Zahlen sind bei der Platzangabe erlaubt');
        return;
      }

      const fields = [] as RegistrationField[];
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

      this.$emit('submit', {slots, slotsWaiting, fields});
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
