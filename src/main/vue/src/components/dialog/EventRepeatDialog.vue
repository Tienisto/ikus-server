<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" title="Event wiederholen">
    <template v-slot:content>
      Beim Wiederholen werden Kopien des Events erstellt.
      Das ursprüngliche Event bleibt unverändert.
      Die Anmeldungen werden nicht kopiert.
      <br><br>

      <v-select
          label="Interval"
          v-model="interval"
          :items="intervalList" item-text="label" item-value="id"
          filled
          return-object
          prepend-icon="mdi-repeat"
      />

      <DatePicker v-model="endDate" label="Enddatum" icon="mdi-calendar" />
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading">
        <v-icon left>mdi-repeat</v-icon>
        Wiederholen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";
import {showSnackbar} from "@/utils";
import DatePicker from "@/components/input/DatePicker.vue";
import {IntervalType} from "@/model";

export default {
  name: 'EventRepeatDialog',
  components: {DatePicker, GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
  },
  data: () => ({
    interval: null,
    intervalList: [],
    endDate: '',
  }),
  methods: {
    reset: function() {
      const initialInterval = IntervalType.WEEKLY;
      this.interval = {
        id: initialInterval,
        label: this.getIntervalLabel(initialInterval)
      };
      this.intervalList = [];
      for (const interval of Object.values(IntervalType)) {
        this.intervalList.push({
          id: interval,
          label: this.getIntervalLabel(interval)
        });
      }

      console.log(this.intervalList);

      // add one week to current date
      const date = new Date();
      date.setDate(date.getDate() + 7);
      this.endDate = date.toISOString().substring(0, 10);
    },
    submit: async function() {
      const interval = this.interval?.id;
      if (!interval) {
        showSnackbar('Bitte wählen Sie ein Intervall aus');
        return;
      }

      const endDate = this.endDate;
      if (!endDate) {
        showSnackbar('Bitte wählen Sie ein Enddatum aus');
        return;
      }

      await this.$emit('submit', interval, endDate);
    },
    getIntervalLabel: function(interval) {
      switch (interval) {
        case IntervalType.WEEKLY:
          return 'Wöchentlich';
        case IntervalType.BIWEEKLY:
          return 'Zweiwöchentlich';
        case IntervalType.MONTHLY:
          return 'Monatlich (Tag im Monat)';
        case IntervalType.MONTHLY_WEEKDAY:
          return 'Monatlich (Wochentag genau)';
        default:
          return 'Unbekannt';
      }
    },
  },
}
</script>
