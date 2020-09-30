<template>
  <MainContainer title="Dashboard" icon="mdi-view-dashboard">

    <template v-slot:intro>
      <div style="display: flex; align-items: flex-start; justify-content: space-between">
        <div>
          Willkommen auf Ihrem Dashboard.
          <br>
          Hier sehen Sie den aktuellen Stand auf einem Blick.
        </div>
        <div>
          <v-btn @click="showAccountSettings" rounded color="primary">
            <v-icon left>mdi-account</v-icon>
            {{ user.name }}
          </v-btn>
        </div>
      </div>
    </template>

    <v-row>
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Letzte Aktivitäten</v-card-title>
          <v-card-text>
            <LoadingIndicator v-if="fetching" />
            <div v-for="(l, index) in dashboard.logs" :key="'l'+index" style="display: flex;">
              <span style="flex: 1; max-width: 460px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
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
              <span style="flex: 1; max-width: 460px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
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
            <Calendar :events="dashboard.events" locale="EN" :calendar-style="{'height': '400px'}"
                      @click:event="showEvent" @click:more="showMoreEvents">
              <template v-slot:header="{ currMonth, prev, next }">
                <div class="mb-2" style="display: flex; align-items: center; justify-content: space-between">
                  <v-btn @click="prev()" color="black" text>
                    <v-icon dark>mdi-chevron-left</v-icon>
                  </v-btn>
                  <span class="text-h6">{{ currMonth }}</span>
                  <v-btn @click="next()" color="black" text>
                    <v-icon dark>mdi-chevron-right</v-icon>
                  </v-btn>
                </div>
              </template>
            </Calendar>
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

    <EventInfoDialog v-model="dialogEvent" :event="event" :locales="locales" />

    <GenericDialog v-model="dialogMoreEvents" title="Events" :width="350">
      <template v-slot:content>
        <v-list shaped>
          <v-list-item @click="showEvent(e)" v-for="e in moreEvents" :key="'li'+e.id">
            <v-list-item-title>
              {{ e.name.en }}
            </v-list-item-title>
            <v-list-item-action>
              <v-icon>mdi-arrow-right</v-icon>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogMoreEvents = false" color="black" text>
          Schließen
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogAccount" title="Passwort ändern">
      <template v-slot:content>
        <v-row>
          <v-col cols="6">
            <v-text-field v-model="oldPassword" label="Aktuelles Passwort" type="password" :disabled="loading" hide-details></v-text-field>
          </v-col>
          <v-col cols="6"></v-col>
          <v-col cols="6">
            <v-text-field v-model="newPassword" label="Neues Passwort" type="password" :disabled="loading"></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-text-field v-model="newPasswordRepeat" label="Passwort wiederholen" type="password" :disabled="loading"></v-text-field>
          </v-col>
        </v-row>
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogAccount = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="updatePassword" color="primary" :loading="loading">
          <v-icon left>mdi-content-save</v-icon>
          Speichern
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script>
import moment from "moment";
import {getDashboard, getUserInfo, isInitialized, updateMyPassword} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import {logTypeString, showSnackbar, sleep} from "@/utils";
import Notice from "@/components/Notice";
import LoadingIndicator from "@/components/LoadingIndicator";
import Calendar from "@/components/Calendar";
import EventInfoDialog from "@/components/dialog/EventInfoDialog";
import GenericDialog from "@/components/dialog/GenericDialog";

export default {
  name: 'DahboardView',
  components: {GenericDialog, EventInfoDialog, Calendar, LoadingIndicator, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    user: {},
    dashboard: {
      logs: [],
      posts: [],
      events: []
    },
    locales: ['EN', 'DE'],
    dialogAccount: false,
    dialogEvent: false,
    dialogMoreEvents: false,
    event: {},
    moreEvents: [],
    oldPassword: '',
    newPassword: '',
    newPasswordRepeat: ''
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      while(!isInitialized()) {
        await sleep(100);
      }
      this.user = getUserInfo();
      this.dashboard = await getDashboard();
      this.fetching = false;
    },
    showAccountSettings: function() {
      this.oldPassword = '';
      this.newPassword = '';
      this.newPasswordRepeat = '';
      this.dialogAccount = true;
    },
    showEvent: function(event) {
      this.event = event;
      this.dialogMoreEvents = false;
      this.dialogEvent = true;
    },
    showMoreEvents: function(events) {
      this.moreEvents = events;
      this.dialogMoreEvents = true;
    },
    updatePassword: async function() {
      if (!this.oldPassword || !this.newPassword || !this.newPasswordRepeat) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      if (this.newPassword !== this.newPasswordRepeat) {
        showSnackbar('Passwörter stimmen nicht überein');
        return;
      }

      try {
        this.loading = true;
        await updateMyPassword({ oldPassword: this.oldPassword, newPassword: this.newPassword });
        this.dialogAccount = false;
      } catch (e) {
        if (e === 403)
          showSnackbar('Falsches Passwort');
        else
          showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
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