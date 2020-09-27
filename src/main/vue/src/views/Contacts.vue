<template>
  <MainContainer title="Kontakte" icon="mdi-card-account-mail">

    <template v-slot:intro>
      Hier können Sie die Kontakte verwalten.
    </template>

    <template v-slot:meta>
      <p class="text-h6">Optionen</p>
      <LocaleSelector v-model="locale" :locales="locales"/>
      <br>
      <v-btn @click="showCreateContact" color="primary" block :disabled="loading">
        <v-icon left>mdi-plus</v-icon>
        Neuer Kontakt
      </v-btn>
    </template>

    <Notice v-if="!fetching && contacts.length === 0"
            title="Es existieren noch keine Kontakte" info="Sie können rechts neue erstellen" />

    <v-row>
      <v-col cols="6" v-for="c in contacts" :key="c.id" class="pt-0 pb-6">
        <v-card>
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
  createContact,
  updateContact,
  deleteContact,
  getContacts,
  moveUpContact,
  moveDownContact
} from "@/api";
import {localizedString, showSnackbar} from "@/utils";
import MainContainer from "@/components/layout/MainContainer";
import LocaleSelector from "@/components/LocaleSelector";
import ContactDialog from "@/components/dialog/ContactDialog";
import GenericDialog from "@/components/dialog/GenericDialog";
import Notice from "@/components/Notice";

export default {
  name: 'ContactView',
  components: {Notice, GenericDialog, ContactDialog, LocaleSelector, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    contacts: [],
    locales: ['EN', 'DE'],
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
    submitContact: async function(contact) {
      if (this.dialogUpdating)
        await this.updateContact(contact);
      else
        await this.createContact(contact);
    },
    createContact: async function(contact) {
      try {
        this.loading = true;
        await createContact(contact);
        this.dialogContact = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateContact: async function(contact) {
      try {
        this.loading = true;
        await updateContact({
          id: this.selectedContact.id,
          ...contact
        });
        this.dialogContact = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
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