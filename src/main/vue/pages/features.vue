<template>
  <MainContainer title="Features" icon="mdi-format-list-bulleted-square">

    <template v-slot:intro>
      Hier können Sie die Liste der Features verwalten.
      <br>
      Neben den voreingestellten Features können Sie Verknüpfungen zu Posts oder Links hinzufügen.
      <br>
      Sie können wichtige Features mit einem Herzchen versehen. Diese Einstellung wird bei <b>neuen</b> Installationen übernommen.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>

      <LocaleSelector v-model="locale" />

      <br>

      <v-btn @click="showCreateDialog" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neuer Eintrag
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && features.length === 0" />

    <v-card v-for="f in features" :key="'f-'+f.id" class="mb-4">
      <v-card-text class="pl-5 pt-3 pb-3">
        <div style="display: flex; align-items: center">
          <div style="flex: 1">
            <div style="display: flex; align-items: center">
              <m-icon :icon="featureIcon(f)" style="font-size: 40px" />

              <span style="flex: 1" class="pl-4">
                <span>{{ featureBadge(f) }}</span>
                <br>
                <span class="text-h6">{{ featureName(f) }}</span>
              </span>

            </div>
          </div>
          <div style="display: flex">

            <v-btn @click="showUpdateDialog(f)" v-if="!f.nativeFeature" icon="mdi-pencil"
                   variant="text" :disabled="loading" />

            <v-btn @click="showDeleteDialog(f)" v-if="!f.nativeFeature" icon="mdi-delete"
                   variant="text" :disabled="loading" />

            <v-btn @click="toggleFavorite(f)" :icon="f.favorite ? 'mdi-heart' : 'mdi-heart-outline'"
                   variant="text" :disabled="loading" />

            <v-btn @click="moveUpFeature(f)" icon="mdi-arrow-up"
                   variant="text" :disabled="loading" />

            <v-btn @click="moveDownFeature(f)" icon="mdi-arrow-down"
                   variant="text" :disabled="loading" />

          </div>
        </div>
      </v-card-text>
    </v-card>

    <FeatureDialog ref="featureDialog" v-model="dialogFeature" :updating="dialogUpdating" :loading="loading"
                   @submit="submitFeature" />

    <GenericDeleteDialog v-model="dialogDelete" dialog-title="Eintrag löschen"
                         @delete="deleteFeature" :loading="loading">
      Folgender Eintrag wird gelöscht:
      <br>
      <b>{{ dialogDelete ? featureName(selectedFeature) : '' }}</b>
      <br><br>
      Möchten Sie wirklich diesen Eintrag löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>

  </MainContainer>
</template>

<script>
import {
  createFeature, deleteFeature,
  getFeatures,
  moveDownFeature,
  moveUpFeature,
  toggleFeatureFavorite, updateFeature
} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import LoadingIndicator from "@/components/LoadingIndicator";
import LocaleSelector from "@/components/LocaleSelector";
import FeatureDialog from "@/components/dialog/FeatureDialog";
import GenericDeleteDialog from "@/components/dialog/GenericDeleteDialog";
import MIcon from "@/components/MIcon";
import {localizedString} from "~/utils/localization";
import {showSnackbar} from "~/utils/snackbar";

export default {
  name: 'FeaturesView',
  components: {MIcon, GenericDeleteDialog, FeatureDialog, LocaleSelector, LoadingIndicator, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    features: [],
    locale: 'EN',
    dialogFeature: false,
    dialogUpdating: false, // true if dialog is used for updating a feature
    dialogDelete: false,
    selectedFeature: {}
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      try {
        this.features = await getFeatures();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.fetching = false;
      }
    },
    showCreateDialog: function() {
      this.$refs.featureDialog.reset(this.locale);
      this.dialogUpdating = false;
      this.dialogFeature = true;
    },
    showUpdateDialog: function(feature) {
      // apply post
      this.selectedFeature = feature;
      this.$refs.featureDialog.reset(this.locale);
      this.$refs.featureDialog.load(feature);
      this.dialogUpdating = true;
      this.dialogFeature = true;
    },
    showDeleteDialog: function(feature) {
      this.selectedFeature = feature;
      this.dialogDelete = true;
    },
    submitFeature: async function(feature) {
      if (this.dialogUpdating)
        await this.updateFeature(feature);
      else
        await this.createFeature(feature);
    },
    createFeature: async function(feature) {
      try {
        this.loading = true;
        await createFeature(feature);
        this.dialogFeature = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateFeature: async function(feature) {
      try {
        this.loading = true;
        await updateFeature({
          id: this.selectedFeature.id,
          ...feature
        });
        this.dialogFeature = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    toggleFavorite: async function(feature) {
      try {
        this.loading = true;
        await toggleFeatureFavorite({ featureId: feature.id });
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveUpFeature: async function(feature) {
      try {
        this.loading = true;
        await moveUpFeature({featureId: feature.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownFeature: async function(feature) {
      try {
        this.loading = true;
        await moveDownFeature({featureId: feature.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteFeature: async function() {
      try {
        this.loading = true;
        await deleteFeature({ featureId: this.selectedFeature.id });
        this.dialogDelete = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
  },
  computed: {
    featureIcon: function() {
      return (feature) => {
        if (feature.nativeFeature) {
          switch(feature.nativeFeature) {
            case 'MAP': return 'map';
            case 'MY_EVENTS': return 'today';
            case 'MENSA': return 'restaurant';
            case 'LINKS': return 'language';
            case 'HANDBOOK': return 'book';
            case 'AUDIO': return 'headset';
            case 'FAQ': return 'help';
            case 'CONTACTS': return 'person';
            case 'EMAILS': return 'email';
            case 'GRADES': return 'check_circle';
            case 'TIMETABLE': return 'table_chart';
            default: return '';
          }
        } else {
          return feature.icon || 'mdi-text-subject';
        }
      }
    },
    featureBadge: function() {
      return (feature) => {
        if (feature.nativeFeature)
          return 'Voreingestellt';

        if (feature.post)
          return 'Post';

        if (feature.link)
          return 'Link';

        return 'Error';
      }
    },
    featureName: function() {
      return (feature) => {
        if (feature.nativeFeature) {
          let stringObj;
          switch(feature.nativeFeature) {
            case 'MAP': stringObj = { 'en': 'Campus', 'de': 'Campus' }; break;
            case 'MY_EVENTS': stringObj = { 'en': 'My Events', 'de': 'My Events' }; break;
            case 'MENSA': stringObj = { 'en': 'Mensa', 'de': 'Mensa' }; break;
            case 'LINKS': stringObj = { 'en': 'Links', 'de': 'Links' }; break;
            case 'HANDBOOK': stringObj = { 'en': 'Handbook', 'de': 'Handbuch' }; break;
            case 'AUDIO': stringObj = { 'en': 'Audio', 'de': 'Audio' }; break;
            case 'FAQ': stringObj = { 'en': 'FAQ', 'de': 'FAQ' }; break;
            case 'CONTACTS': stringObj = { 'en': 'Contacts', 'de': 'Kontakte' }; break;
            case 'EMAILS': stringObj = { 'en': 'Emails', 'de': 'E-Mails' }; break;
            case 'GRADES': stringObj = { 'en': 'Grades', 'de': 'Noten' }; break;
            case 'TIMETABLE': stringObj = { 'en': 'Timetable', 'de': 'Stundenplan' }; break;
          }
          return localizedString(stringObj, this.locale);
        } else {
          return localizedString(feature.name, this.locale);
        }
      };
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>
