<template>
  <MainContainer title="Kontakte" icon="mdi-card-account-mail">

    <template v-slot:intro>
      Hier können Sie die Kontakte verwalten.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" />
      <br>
      <v-btn @click="showCreateContact" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neuer Kontakt
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && contacts.length === 0" />

    <Notice v-if="!fetching && contacts.length === 0"
            title="Es existieren noch keine Kontakte" info="Sie können rechts neue erstellen" />

    <v-row>
      <v-col cols="6" v-for="c in contacts" :key="c.id" class="pt-0 pb-6">
        <v-card style="height: 100%">
          <v-card-title>
            <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
              <span>
                {{ localized(c.name) }}
              </span>
              <span>
                <v-tooltip top>
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn @click="moveUpContact(c)" :disabled="loading" v-bind="attrs" v-on="on" icon small>
                      <v-icon>mdi-arrow-up</v-icon>
                    </v-btn>
                  </template>
                  <span>Nach oben</span>
                </v-tooltip>
                <v-tooltip top>
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn @click="moveDownContact(c)" :disabled="loading" class="ml-2" v-bind="attrs" v-on="on" icon small>
                      <v-icon>mdi-arrow-down</v-icon>
                    </v-btn>
                  </template>
                  <span>Nach unten</span>
                </v-tooltip>
                <v-tooltip top>
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn @click="showUpdateContact(c)" :disabled="loading" class="ml-2" v-bind="attrs" v-on="on" icon small>
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                  <span>Bearbeiten</span>
                </v-tooltip>
                <v-tooltip top>
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn @click="showDeleteContact(c)" :disabled="loading" class="ml-2" v-bind="attrs" v-on="on" icon small>
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                  <span>Löschen</span>
                </v-tooltip>
              </span>
            </div>
          </v-card-title>

          <v-card-text>

            <div v-if="c.file" class="mb-4" style="display: inline-block">
              <v-card class="secondary">
                <v-card-text class="pa-2">
                  <v-img v-if="c.file" :src="fileUrl(c.file)" :width="100" :height="100" />
                </v-card-text>
              </v-card>
            </div>

            <table>
              <colgroup>
                <col style="width: 40px;">
                <col>
              </colgroup>
              <tbody class="contact-info">
                <tr v-if="c.place">
                  <td>
                    <v-icon>mdi-map-marker</v-icon>
                  </td>
                  <td v-html="place(c)">
                  </td>
                </tr>

                <tr v-if="c.email">
                  <td>
                    <v-icon>mdi-email</v-icon>
                  </td>
                  <td>
                    {{ c.email }}
                  </td>
                </tr>

                <tr v-if="c.phoneNumber">
                  <td>
                    <v-icon>mdi-phone</v-icon>
                  </td>
                  <td>
                    {{ c.phoneNumber }}
                  </td>
                </tr>

                <tr v-if="c.openingHours">
                  <td>
                    <v-icon>mdi-clock-outline</v-icon>
                  </td>
                  <td>
                    {{ localized(c.openingHours) }}
                  </td>
                </tr>

                <tr v-if="c.links.length !== 0">
                  <td>
                    <v-icon>mdi-web</v-icon>
                  </td>
                  <td>
                    <div v-for="(l, index) in c.links" :key="'l' + index">
                      <a :href="l" target="_blank" style="text-decoration: none">{{ l }}</a>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <ContactDialog ref="contactDialog" v-model="dialogContact"
                   :updating="dialogUpdating" :loading="loading"
                   @submit="submitContact"/>

    <GenericDialog v-model="dialogDelete" title="Kontakt löschen">
      <template v-slot:content>
        Folgender Kontakt wird gelöscht:
        <br>
        <b>{{ selectedContact.name ? selectedContact.name.en : '' }}</b>
        <br><br>
        Möchten Sie wirklich diesen Kontakt löschen?
        <br>
        Dieser Vorgang ist nicht widerrufbar.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDelete = false" color="black" text :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deleteContact" color="primary" :loading="loading">
          <v-icon left>mdi-delete</v-icon>
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script>
import {
  getContacts,
  createContact, updateContact, deleteContact,
  moveUpContact, moveDownContact,
  setContactFile, deleteContactFile, getFileUrl
} from "@/api";
import {localizedString, showSnackbar} from "@/utils";
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import ContactDialog from "@/components/dialog/ContactDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import Notice from "@/components/Notice";
import LoadingIndicator from "@/components/LoadingIndicator";

export default {
  name: 'ContactView',
  components: {LoadingIndicator, Notice, GenericDialog, ContactDialog, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    contacts: [],
    locale: 'EN',
    dialogContact: false,
    dialogUpdating: false,
    dialogDelete: false,
    selectedContact: {},
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.contacts = await getContacts();
      this.fetching = false;
    },
    showCreateContact: function() {
      this.$refs.contactDialog.reset();
      this.dialogUpdating = false;
      this.dialogContact = true;
    },
    showUpdateContact: function(contact) {
      // apply
      this.selectedContact = contact;
      this.$refs.contactDialog.reset();
      this.$refs.contactDialog.load(contact);
      this.dialogUpdating = true;
      this.dialogContact = true;
    },
    showDeleteContact: function(contact) {
      this.selectedContact = contact;
      this.dialogDelete = true;
    },
    submitContact: async function(contact, options) {
      if (this.dialogUpdating)
        await this.updateContact(contact, options);
      else
        await this.createContact(contact, options);
    },
    createContact: async function(contact, options) {
      try {
        this.loading = true;
        const savedContact = await createContact(contact);
        await this.handleOptions(savedContact.id, options);
        this.dialogContact = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateContact: async function(contact, options) {
      try {
        this.loading = true;
        await updateContact({
          id: this.selectedContact.id,
          ...contact
        });
        await this.handleOptions(this.selectedContact.id, options);
        this.dialogContact = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    handleOptions: async function(contactId, options) {
      if (options.uploadingFile) {
        await setContactFile({ contactId, file: options.uploadingFile });
      } else if (options.deletingFile) {
        await deleteContactFile({ contactId });
      }
    },
    moveUpContact: async function(contact) {
      try {
        this.loading = true;
        await moveUpContact({id: contact.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    moveDownContact: async function(contact) {
      try {
        this.loading = true;
        await moveDownContact({id: contact.id});
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteContact: async function() {
      try {
        this.loading = true;
        await deleteContact({ id: this.selectedContact.id });
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
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    },
    place: function() {
      return (contact) => {
        return contact.place.replaceAll('\n','<br>');
      }
    },
    fileUrl: function() {
      return (file) => getFileUrl(file) + '#' + new Date().getTime();
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>

<style>
.contact-info td {
  padding-bottom: 0.5rem;
  vertical-align: top;
}
</style>