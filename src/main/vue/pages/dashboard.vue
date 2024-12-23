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
          <v-btn @click="showAccountSettings" color="primary" prepend-icon="mdi-account">
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
            <router-link to="/activities" v-slot="{ href, navigate }" custom>
              <v-btn @click="navigate" :href="href" color="black" variant="text">
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
            <router-link to="/posts" v-slot="{ href, navigate }" custom>
              <v-btn @click="navigate" :href="href" color="black" variant="text">
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
            <Calendar :events="dashboard.events" locale="EN"
                      @click:event="showEvent" @click:more="showMoreEvents" />
          </v-card-text>

          <v-card-actions>
            <v-spacer/>
            <router-link to="/calendar" v-slot="{ href, navigate }" custom>
              <v-btn @click="navigate" :href="href" color="black" variant="text">
                Mehr
                <v-icon right>mdi-arrow-right</v-icon>
              </v-btn>
            </router-link>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <EventInfoDialog v-model="dialogEvent" :event="event" />

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
        <v-btn @click="dialogMoreEvents = false" color="black" variant="text">
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
        <v-btn @click="dialogAccount = false" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="updatePassword" color="primary" :loading="loading" prepend-icon="mdi-content-save" variant="elevated">
          Speichern
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script lang="ts">
import {getDashboard, getUserInfo, isInitialized, updateMyPassword} from "~/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import Notice from "@/components/Notice.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import Calendar from "@/components/Calendar.vue";
import EventInfoDialog from "@/components/dialog/EventInfoDialog.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import {showSnackbar} from "@/utils/snackbar";
import {logTypeString} from "@/utils/logType";
import type {DashboardDto, EventDto, MeDto} from "@/models";
import {sleep} from "@/utils/sleep";
import moment from "moment";

export default {
  name: 'DahboardView',
  components: {GenericDialog, EventInfoDialog, Calendar, LoadingIndicator, Notice, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    user: {} as MeDto,
    dashboard: {
      logs: [],
      posts: [],
      events: []
    } as DashboardDto,
    dialogAccount: false,
    dialogEvent: false,
    dialogMoreEvents: false,
    event: {} as EventDto,
    moreEvents: [] as EventDto[],
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
      this.user = getUserInfo()!;
      this.dashboard = await getDashboard();
      this.fetching = false;
    },
    showAccountSettings: function() {
      this.oldPassword = '';
      this.newPassword = '';
      this.newPasswordRepeat = '';
      this.dialogAccount = true;
    },
    showEvent: function(event: EventDto) {
      this.event = event;
      this.dialogMoreEvents = false;
      this.dialogEvent = true;
    },
    showMoreEvents: function(events: EventDto[]) {
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
      return (time: string) => moment(time).format('ddd, DD.MM.YYYY');
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>
