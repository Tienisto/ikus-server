<template>
  <div>
    <slot name="header" :curr-month="currMonth"></slot>
    <v-calendar ref="calendar" locale="de" color="primary" :weekdays="[1, 2, 3, 4, 5, 6, 0]"
                v-model="today" :events="internalEvents" event-start="start" event-end="end"
                :interval-format="intervalFormat"
                @click:event="$emit('click:event', $event.event)" @click:date="$emit('click:day', $event)"
                :style="{'min-height': $vuetify.breakpoint.lgAndUp ? '500px' : '400px'}">

      <template v-slot:event="{ event }">
        <span class="font-weight-bold ml-1">{{ eventTime(event) }}</span>
        {{ eventName(event) }}
      </template>
    </v-calendar>
  </div>
</template>

<script>
import {localizedString} from "@/utils";
import moment from "moment";

export default {
  name: 'Calendar',
  props: {
    events: {
      type: Array,
      default: () => []
    },
    locale: {
      type: String,
      default: 'EN'
    }
  },
  data: () => ({
    today: new Date()
  }),
  methods: {
    prev: function() {
      this.$refs.calendar.prev();
    },
    next: function() {
      this.$refs.calendar.next();
    },
    digestEvent: function(event) {
      const start = moment(event.startTime).format('YYYY-MM-DD HH:mm');
      const end = event.endTime ? moment(event.endTime).format('YYYY-MM-DD HH:mm') : null;
      return { start, end, ...event };
    },
    intervalFormat: function(obj) {
      console.log(obj);
      return obj.time;
    }
  },
  computed: {
    currMonth: function() {
      return moment(this.today).format('MMMM YYYY');
    },
    internalEvents: function() {
      return this.events.map((e) => this.digestEvent(e));
    },
    eventTime: function() {
      return (event) => {
        const time = moment(event.startTime);
        if (time.minutes() === 0)
          return moment(event.startTime).format('HH');
        else
          return moment(event.startTime).format('HH:mm');
      }
    },
    eventName: function() {
      return (event) => {
        return localizedString(event.name, this.locale);
      }
    }
  }
}
</script>