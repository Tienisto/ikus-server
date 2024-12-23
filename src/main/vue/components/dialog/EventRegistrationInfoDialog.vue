<template>
  <GenericDialog v-model="value" title="Anmeldungsinformation">
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
      <b>Geburtsdatum: </b>
      <br>
      {{ dateOfBirth }}
      <br><br>
      <b>Zeitstempel: </b>
      <br>
      {{ timestamp }}
    </template>

    <template v-slot:actions>
      <v-btn @click="value = false">
        Schließen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script lang="ts" setup>
import moment from "moment"
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import type {RegistrationData} from "@/models";
import {computed} from "vue";

const props = defineProps<{
  modelValue: boolean,
  data: RegistrationData
}>();

const dateOfBirth = computed(() => {
  return props.data.dateOfBirth ? moment(props.data.dateOfBirth).format('DD.MM.YYYY') : '(keine Angabe)';
});

const timestamp = computed(() => {
  return moment(props.data.timestamp).format('DD.MM.YYYY, HH:mm');
});

const emit = defineEmits(['update:modelValue']);
const value = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});
</script>
