<template>
  <MainContainer title="Dashboard" icon="mdi-view-dashboard">

    <template v-slot:intro>
      Willkommen auf Ihrem Dashboard.
      <br>
      Hier sehen Sie den aktuellen Stand auf einem Blick.
    </template>

    <v-row>
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Letzte Aktivitäten</v-card-title>
          <v-card-text>
            <LoadingIndicator v-if="fetching" />
            <div v-for="(l, index) in dashboard.logs" :key="'l'+index" style="display: flex;">
              <span style="flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                <b>{{ l.user ? l.user.name+':' : '' }}</b> {{ typeString(l.type) }}, {{ l.info }}
              </span>
              <span>{{ timeString(l.timestamp) }}</span>
            </div>
          </v-card-text>

          <v-card-actions>
            <v-spacer/>
            <router-link to="/logs" v-slot="{ href, navigate }">
              <v-btn @click="navigate" :href="href" color="black" text>
                Mehr
                <v-icon right>mdi-arrow-right</v-icon>
              </v-btn>
            </router-link>
          </v-card-actions>
        </v-card>

        <v-card class="mt-6">
          <v-card-title>Neuste Posts</v-card-title>
          <v-card-text>
            <LoadingIndicator v-if="fetching" />
            <Notice v-if="!fetching && dashboard.posts.length === 0" title="Noch keine Posts veröffentlicht."/>
            <div v-for="p in dashboard.posts" :key="'p'+p.id" style="display: flex;">
              <span style="flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                <b>{{ p.channel.name.de }}:</b> {{ p.title.de }}
              </span>
              <span>{{ timeString(p.date) }}</span>
            </div>
          </v-card-text>

          <v-card-actions>
            <v-spacer/>
            <router-link to="/posts" v-slot="{ href, navigate }">
              <v-btn @click="navigate" :href="href" color="black" text>
                Mehr
                <v-icon right>mdi-arrow-right</v-icon>
              </v-btn>
            </router-link>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card style="height: 100%">
          <v-card-title>Kalender</v-card-title>
          <v-card-text>
            <div style="display: flex; align-items: center; justify-content: space-between">
              <v-btn @click="$refs.calendar.prev()" color="black" text>
                <v-icon dark>mdi-chevron-left</v-icon>
              </v-btn>
              <span class="black--text">{{ moment(today).format('MMMM YYYY') }}</span>
              <v-btn @click="$refs.calendar.next()" color="black" text>
                <v-icon dark>mdi-chevron-right</v-icon>
              </v-btn>
            </div>
            <v-calendar ref="calendar" locale="de" color="primary" :weekdays="[1, 2, 3, 4, 5, 6, 0]"
                        v-model="today"/>
          </v-card-text>

          <v-card-actions>
            <v-spacer/>
            <router-link to="/calendar" v-slot="{ href, navigate }">
              <v-btn @click="navigate" :href="href" color="black" text>
                Mehr
                <v-icon right>mdi-arrow-right</v-icon>
              </v-btn>
            </router-link>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>


  </MainContainer>
</template>

<script>
import {getDashboard} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import {logTypeString} from "@/utils";
import moment from "moment";
import Notice from "@/components/Notice";
import LoadingIndicator from "@/components/LoadingIndicator";

export default {
  name: 'DahboardView',
  components: {LoadingIndicator, Notice, MainContainer},
  data: () => ({
    fetching: true,
    dashboard: {
      logs: [],
      posts: []
    },
    today: new Date(),
    moment: moment
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.dashboard = await getDashboard();
      this.fetching = false;
    }
  },
  computed: {
    typeString: function() {
      return logTypeString;
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>