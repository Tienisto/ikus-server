<template>
  <ClientOnly>
    <FullCalendar :options="calendarOptions" />
  </ClientOnly>
</template>

<script setup lang="ts">
import type {EventDto} from "@/models";
import {ref, watch} from "vue";
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import deLocale from '@fullcalendar/core/locales/de';
import {type CalendarOptions} from "@fullcalendar/core";

interface FullCalendarEvent {
  id: number;
  title: string;
  start: string;
  end: string;
}

const props = defineProps<{
  events: EventDto[],
  locale: string,
}>();

const emit = defineEmits<{
  (e: 'click:date', date: string): void
  (e: 'click:event', event: EventDto): void
}>();

const calendarOptions = ref({
  locale: deLocale,
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  dateClick: (date: any) => emit('click:date', date.dateStr),
  eventClick: (event: any) => {
    emit('click:event', props.events.find(e => {
      // FullCalendar always converts the id to string although their docs say it can be a number or string
      return e.id.toString() === event.event.id;
    })!);
  },
  events: [],
  eventDisplay: 'block',
  eventColor: '#7a003f',
  eventTimeFormat: { // like '14:30:00'
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  },
  headerToolbar: {
    left: 'title',
    center: '',
    right: 'prev,next'
  },
} as CalendarOptions);

watch(() => [props.events, props.locale], () => {
  // @ts-ignore: events is a FullCalendarEvent[]
  calendarOptions.value.events = props.events.map((e) => ({
    id: e.id,
    title: props.locale === 'EN' ? e.name.en : e.name.de,
    start: e.startTime,
    end: e.endTime,
  } as FullCalendarEvent));
}, {immediate: true});
</script>

<style>
.fc .fc-button-primary {
  background-color: #7a003f !important;
  border-color: #7a003f !important;
  border-radius: 4px !important;
  margin: 0 4px !important;
}

.fc .fc-event {
  cursor: pointer;
}
</style>
