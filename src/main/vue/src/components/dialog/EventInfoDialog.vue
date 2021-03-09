<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="eventName">
    <template v-slot:content>
      <br>
      <div style="display: flex; align-items: flex-start; justify-content: space-between">
        <div>
          <b>Kanal:</b>
          <br>
          {{ eventChannel }}
        </div>
        <LocaleSelector v-model="locale" />
      </div>
      <br>
      <b>Information:</b>
      <br>
      {{ eventInfo }}
      <br><br>
      <b>Wann: </b>
      <br>
      {{ eventTime }}
      <br><br>
      <b>Wo: </b>
      <br>
      {{ event.place }}
      <br>
      {{ eventCoords }}
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
import LocaleSelector from "@/components/LocaleSelector";
import {localizedString} from "@/utils";

export default {
  name: 'EventInfoDialog',
  components: {LocaleSelector, GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    event: {
      type: Object,
      required: true
    }
  },
  data: () => ({
    locale: 'EN',
  }),
  computed: {
    eventName: function() {
      if (this.event.name)
        return localizedString(this.event.name, this.locale);
      else
        return '';
    },
    eventChannel: function() {
      if (this.event.channel)
        return localizedString(this.event.channel.name, this.locale);
      else
        return '';
    },
    eventInfo: function() {
      if (this.event.info)
        return localizedString(this.event.info, this.locale);
      else
        return '(leer)';
    },
    eventTime: function() {
      if (!this.event.startTime && !this.event.endTime)
        return '(leer)';

      const start = moment(this.event.startTime);
      const end = this.event.endTime ? moment(this.event.endTime) : null;

      if (start && end) {
        if (start.date() === end.date() && start.month() === end.month() && start.year() === end.year()) {
          // same day
          return start.format('DD.MM.YYYY, HH:mm') + ' - ' + end.format('HH:mm');
        } else {
          // multiple days
          return start.format('DD.MM.YYYY, HH:mm') + ' - ' + end.format('DD.MM.YYYY, HH:mm');
        }
      } else {
        return start.format('DD.MM.YYYY, HH:mm');
      }
    },
    eventCoords: function() {
      if (this.event.coords)
        return '(' + this.event.coords.x + ', ' + this.event.coords.y + ')';
      else
        return '';
    }
  }
}
</script>