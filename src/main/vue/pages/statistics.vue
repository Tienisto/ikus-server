<template>
  <MainContainer title="Statistiken" icon="mdi-finance">

    <template v-slot:intro>
      Beim Start der Welcome to OVGU - App werden Daten an dem Server gesendet, die im Anschluss anonymisiert abgespeichert werden.
      <br>
      Diese Daten sind hilfreich, damit Sie hier konkrete Zahlen zur App-Nutzung einsehen können.
      <br>
      Eine Einheit entspricht ein Nutzer bzw. ein Gerät.
    </template>

    <v-row>
      <v-col cols="4">
        <NumberCard label="Diesen Monat" :number="curr.month" :animation-duration="3000" />
      </v-col>
      <v-col cols="4">
        <NumberCard label="Diese Woche" :number="curr.week" :animation-duration="3000" />
      </v-col>
      <v-col cols="4">
        <NumberCard label="Heute" :number="curr.day" :animation-duration="3000" />
      </v-col>
    </v-row>

    <v-card class="mt-4">
      <v-card-title>Verlauf</v-card-title>
      <v-card-text>
        <BarChart v-if="stats" :chart-data="chartData" :options="chartOptions" :height="300" />
        <div v-else style="height: 300px"></div>
      </v-card-text>

      <v-divider />

      <v-card-actions>
        <v-btn @click="platform = null" color="primary" :class="!platform ? ['v-btn--active'] : []" variant="text">
          Alle
        </v-btn>
        <v-btn @click="platform = 'ANDROID'" color="primary" :class="platform === 'ANDROID' ? ['v-btn--active'] : []" variant="text">
          Android
        </v-btn>
        <v-btn @click="platform = 'IOS'" color="primary" :class="platform === 'IOS' ? ['v-btn--active'] : []" variant="text">
          iOS
        </v-btn>
        <v-spacer />
        <v-btn @click="setChartMode('MONTHLY')" color="primary" :class="mode === 'MONTHLY' ? ['v-btn--active'] : []" variant="text">
          Monat
        </v-btn>
        <v-btn @click="setChartMode('WEEKLY')" color="primary" :class="mode === 'WEEKLY' ? ['v-btn--active'] : []" variant="text">
          Woche
        </v-btn>
        <v-btn @click="setChartMode('DAILY')" color="primary" :class="mode === 'DAILY' ? ['v-btn--active'] : []" variant="text">
          Tag
        </v-btn>
      </v-card-actions>

    </v-card>

  </MainContainer>
</template>

<script lang="ts">
import moment from 'moment';
import {getStats, getStatsCurr, StatsType} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import NumberCard from "@/components/NumberCard";
import BarChart from "@/components/charts/BarChart";
import type {AppStartDto, CurrentAppStarts} from "~/models";

export default {
  name: 'StatisticsView',
  components: {BarChart, NumberCard, MainContainer},
  data: () => ({
    fetching: true,
    stats: null as AppStartDto[] | null,
    curr: {
      month: 0,
      week: 0,
      day: 0
    } as CurrentAppStarts,
    mode: 'DAILY' as StatsType,
    platform: null as null | 'ANDROID' | 'IOS',
    timerId: null
  }),
  methods: {
    setChartMode: async function(mode) {
      this.mode = mode;
      this.stats = await getStats({ type: this.mode });
    },
    updateCurr: async function() {
      this.curr = await getStatsCurr();
    }
  },
  computed: {
    chartData: function() {
      return {
        labels: this.stats.map((s) => {
          switch(this.mode) {
            case 'DAILY':
              return moment(s.date).subtract(1, 'days').format('DD.MM.');
            case 'WEEKLY':
              return moment(s.date).subtract(7, 'days').format('DD.MM.');
            case 'MONTHLY':
              return moment(s.date).subtract(1, 'month').format('MM / YYYY');
          }
        }),
        datasets: [{
          data: this.stats.map((s) => {
            switch(this.platform) {
              case 'ANDROID':
                return s.android;
              case 'IOS':
                return s.ios;
              default:
                return s.android + s.ios;
            }
          }),
          backgroundColor: '#878787'
        }]
      };
    },
    chartOptions: function() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            title: {
              display: true,
              text: 'Nutzer',
            },
          },
          x: {
            ticks: {
              autoSkip: true,
              autoSkipPadding: 10
            }
          }
        }
      };
    }
  },
  mounted: async function() {
    this.stats = await getStats({ type: this.mode });
    this.curr = await getStatsCurr();
    this.fetching = false;

    this.timerId = setInterval(() => {
      this.updateCurr();
    }, 3000);
  },
  beforeUnmount: function() {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }
}
</script>
