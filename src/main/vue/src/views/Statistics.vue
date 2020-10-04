<template>
  <MainContainer title="Statistiken" icon="mdi-finance">

    <template v-slot:intro>
      Beim Start der Welcome at OVGU - App werden Daten an dem Server gesendet, die im Anschluss anonymisiert abgespeichert werden.
      <br>
      Diese Daten sind hilfreich, damit Sie hier konkrete Zahlen zur App-Nutzung einsehen können.
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
        <v-btn @click="platform = null" color="primary" :class="!platform ? ['v-btn--active'] : []" text>
          Alle
        </v-btn>
        <v-btn @click="platform = 'ANDROID'" color="primary" :class="platform === 'ANDROID' ? ['v-btn--active'] : []" text>
          Android
        </v-btn>
        <v-btn @click="platform = 'IOS'" color="primary" :class="platform === 'IOS' ? ['v-btn--active'] : []" text>
          iOS
        </v-btn>
        <v-spacer />
        <v-btn @click="setChartMode('MONTHLY')" color="primary" :class="mode === 'MONTHLY' ? ['v-btn--active'] : []" text>
          Monat
        </v-btn>
        <v-btn @click="setChartMode('WEEKLY')" color="primary" :class="mode === 'WEEKLY' ? ['v-btn--active'] : []" text>
          Woche
        </v-btn>
        <v-btn @click="setChartMode('DAILY')" color="primary" :class="mode === 'DAILY' ? ['v-btn--active'] : []" text>
          Tag
        </v-btn>
      </v-card-actions>

    </v-card>

  </MainContainer>
</template>

<script>
import moment from 'moment';
import {getStats, getStatsCurr} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import NumberCard from "@/components/NumberCard";
import BarChart from "@/components/charts/BarChart";

export default {
  name: 'StatisticsView',
  components: {BarChart, NumberCard, MainContainer},
  data: () => ({
    fetching: true,
    stats: null,
    curr: {
      month: 0,
      week: 0,
      day: 0
    },
    mode: 'DAILY', // [ MONTHLY, WEEKLY, DAILY ]
    platform: null, // [ null, android, ios ]
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
              return moment(s.date).subtract(1, 'days').format('DD.MM.YYYY');
            case 'WEEKLY':
              return moment(s.date).subtract(7, 'days').format('DD.MM.YYYY');
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
        legend: { display: false },
        scales: {
          yAxes: [{
            scaleLabel: { display: true, labelString: 'Nutzer' },
            ticks: { beginAtZero: true }
          }],
          xAxes: [{
              ticks: {
                autoSkip: true,
                autoSkipPadding: 10
              }
          }]
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
  destroyed: function() {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }
}
</script>