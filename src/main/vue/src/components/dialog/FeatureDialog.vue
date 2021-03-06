<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="dialogTitle">
    <template v-slot:content>

      Der Eintrag wird mit einem Post oder Link verknüpft.
      <br>
      Diesen müssen Sie <b>vorher</b> erstellen.
      <br>
      Beachten Sie, dass der Name kurz sein muss!
      <br><br>

      <v-row>
        <v-col cols="6">
          <v-text-field v-model="nameEn" label="Name (englisch)" :disabled="loading" filled hide-details />
          <br>
          <v-text-field v-model="nameDe" label="Name (deutsch)" :disabled="loading" filled hide-details />
        </v-col>
        <v-col cols="6">
          <v-card class="secondary" style="height: 100%">
            <v-card-text style="height: 100%">
              <div style="height: 100%; display: flex; align-items: center; justify-content: center">
                <m-icon @click="showIconChooser" :icon="icon" style="font-size: 70px; cursor: pointer" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <div style="display: flex; align-items: center; justify-content: space-between">
        <p class="text-h6 mt-6">Verknüpft mit</p>
        <LocaleSelector v-model="locale" />
      </div>
      <div v-if="!post && !link" class="mt-6 mb-4" style="display: flex; align-items: center; justify-content: space-evenly">
        <v-btn @click="showSearchPost" rounded>
          <v-icon left>mdi-pencil</v-icon>
          Post
        </v-btn>

        <v-btn @click="showSearchLink" rounded>
          <v-icon left>mdi-web</v-icon>
          Link
        </v-btn>
      </div>
      <div v-else>
        <v-card class="secondary mb-4">
          <v-card-text>
            <div style="display: flex; align-items: center;">
              <div v-if="post" style="flex: 1">
                <span class="font-weight-bold">{{ localized(post.title) }}</span>
                <br>
                <span>{{ timeString(post.date) }} / {{ localized(post.channel.name) }}</span>
              </div>
              <div v-else style="flex: 1">
                <span class="font-weight-bold">{{ localized(link.info) }}</span>
                <br>
                <span>{{ localized(link.url) }}</span>
              </div>
              <v-btn @click="resetConnection" icon>
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </div>

      <IconChooserDialog ref="iconChooserDialog" v-model="dialogIconChooser" @select="selectIcon" />
      <SearchPostDialog ref="searchPostDialog" v-model="dialogSearchPost" :locale="locale"
                        @select="selectPost" />
      <SearchLinkDialog ref="searchLinkDialog" v-model="dialogSearchLink" :locale="locale"
                        @select="selectLink" />

    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Abbrechen
      </v-btn>
      <v-btn @click="submit" color="primary" :loading="loading">
        <v-icon left>{{ submitIcon }}</v-icon>
        {{ submitText }}
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";
import {localizedString, showSnackbar} from "@/utils";
import SearchPostDialog from "@/components/dialog/SearchPostDialog";
import moment from "moment";
import SearchLinkDialog from "@/components/dialog/SearchLinkDialog";
import IconChooserDialog from "@/components/dialog/IconChooserDialog";
import MIcon from "@/components/MIcon";
import LocaleSelector from "@/components/LocaleSelector";

export default {
  name: 'FeatureDialog',
  components: {LocaleSelector, MIcon, IconChooserDialog, SearchLinkDialog, SearchPostDialog, GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    updating: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  },
  data: () => ({
    locale: 'EN',
    nameEn: '',
    nameDe: '',
    icon: '',
    post: null,
    link: null,
    dialogIconChooser: false,
    dialogSearchPost: false,
    dialogSearchLink: false
  }),
  methods: {
    showIconChooser: function() {
      this.$refs.iconChooserDialog.reset();
      this.dialogIconChooser = true;
    },
    showSearchPost: function() {
      this.$refs.searchPostDialog.reset();
      this.dialogSearchPost = true;
    },
    showSearchLink: function() {
      this.$refs.searchLinkDialog.reset();
      this.dialogSearchLink = true;
    },
    selectIcon: function(icon) {
      this.icon = icon;
      this.dialogIconChooser = false;
    },
    selectPost: function(post) {
      this.post = post;
      this.dialogSearchPost = false;
    },
    selectLink: function(link) {
      this.link = link;
      this.dialogSearchLink = false;
    },
    resetConnection: function() {
      this.post = null;
      this.link = null;
    },
    reset: function(locale) {
      // reset input
      this.nameEn = '';
      this.nameDe = '';
      this.icon = 'description';
      this.post = null;
      this.link = null;

      // apply preset
      this.locale = locale;
    },
    load: function(feature) {
      // apply
      this.nameEn = feature.name.en;
      this.nameDe = feature.name.de;
      this.icon = feature.icon;
      this.post = feature.post;
      this.link = feature.link;
    },
    submit: async function() {

      if (!this.nameEn || !this.nameDe) {
        showSnackbar('Bitte Namen eingeben');
        return;
      }

      if (!this.post && !this.link) {
        showSnackbar('Bitte Verknüpfung festlegen');
        return;
      }

      await this.$emit('submit', {
        name: {
          en: this.nameEn,
          de: this.nameDe
        },
        icon: this.icon,
        postId: this.post ? this.post.id : null,
        linkId: this.link ? this.link.id : null
      });
    }
  },
  computed: {
    dialogTitle: function() {
      return this.updating ? 'Eintrag bearbeiten' : 'Neuer Eintrag';
    },
    submitText: function() {
      return this.updating ? 'Speichern' : 'Erstellen';
    },
    submitIcon: function() {
      return this.updating ? 'mdi-content-save' : 'mdi-plus';
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    },
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    }
  }
}
</script>