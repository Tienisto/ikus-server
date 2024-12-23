<template>
  <MainContainer title="Kontakte" icon="mdi-card-account-mail">

    <template v-slot:intro>
      Hier können Sie die Kontakte verwalten.
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">Optionen</p>

      <LocaleSelector v-model="locale"/>
      <br>
      <v-btn @click="showCreateContact" color="primary" block :disabled="loading" prepend-icon="mdi-plus">
        Neuer Kontakt
      </v-btn>
    </template>

    <LoadingIndicator v-if="fetching && contacts.length === 0"/>

    <Notice v-if="!fetching && contacts.length === 0"
            title="Es existieren noch keine Kontakte" info="Sie können rechts neue erstellen"/>

    <v-row>
      <v-col cols="6" v-for="c in contacts" :key="c.id" class="pt-0 pb-6">
        <v-card style="height: 100%">
          <v-card-title>
            <div style="width: 100%; display: flex; align-items: center;">
              <span class="flex-grow-1">
                {{ localized(c.name) }}
              </span>
              <span>
                <v-tooltip text="Nach oben" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn v-bind="props" @click="moveUp(c)" :disabled="loading" icon="mdi-arrow-up"
                           variant="text" size="small">
                    </v-btn>
                  </template>
                </v-tooltip>

                <v-tooltip text="Nach unten" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn v-bind="props" @click="moveDown(c)" :disabled="loading" icon="mdi-arrow-down"
                           variant="text" size="small">
                    </v-btn>
                  </template>
                </v-tooltip>

                <v-tooltip text="Bearbeiten" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn v-bind="props" @click="showUpdateContact(c)" :disabled="loading" icon="mdi-pencil"
                           variant="text" size="small">
                    </v-btn>
                  </template>
                </v-tooltip>

                <v-tooltip text="Löschen" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn v-bind="props" @click="showDeleteContact(c)" :disabled="loading" icon="mdi-delete"
                           variant="text" size="small">
                    </v-btn>
                  </template>
                </v-tooltip>
              </span>
            </div>
          </v-card-title>

          <v-card-text>

            <div v-if="c.file" class="mb-4" style="display: inline-block">
              <v-card class="secondary">
                <v-card-text class="pa-2">
                  <v-img v-if="c.file" :src="fileUrl(c.file)" :width="100" :height="100"/>
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

    <ContactDialog ref="contact-dialog" v-model="dialogContact"
                   :updating="dialogUpdating" :loading="loading"
                   @submit="submitContact"/>

    <GenericDeleteDialog v-model="dialogDelete" dialog-title="Kontakt löschen"
                         @delete="deleteSelectedContact" :loading="loading">
      Folgender Kontakt wird gelöscht:
      <br>
      <b>{{ selectedContact.name ? selectedContact.name.en : '' }}</b>
      <br><br>
      Möchten Sie wirklich diesen Kontakt löschen?
      <br>
      Dieser Vorgang ist nicht widerrufbar.
    </GenericDeleteDialog>
  </MainContainer>
</template>

<script lang="ts" setup>
import {
  getContacts,
  createContact, updateContact, deleteContact,
  moveUpContact, moveDownContact,
  setContactFile, deleteContactFile, getFileUrl
} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import LocaleSelector from "@/components/LocaleSelector.vue";
import ContactDialog from "@/components/dialog/ContactDialog.vue";
import Notice from "@/components/Notice.vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import {showSnackbar} from "~/utils/snackbar";
import {localizedString} from "~/utils/localization";
import GenericDeleteDialog from "~/components/dialog/GenericDeleteDialog.vue";
import type {ContactDto, MultiLocaleString} from "~/models";
import type {ContactPostAction} from "~/components/dialog/ContactDialog.vue";
import {onMounted, ref, useTemplateRef} from "vue";

const refContactDialog = useTemplateRef<InstanceType<typeof ContactDialog>>('contact-dialog');

const fetching = ref(true);
const loading = ref(false);
const contacts = ref([] as ContactDto[]);
const locale = ref('EN');
const dialogContact = ref(false);
const dialogUpdating = ref(false);
const dialogDelete = ref(false);
const selectedContact = ref({} as ContactDto);

async function fetchData() {
  fetching.value = true;
  contacts.value = await getContacts();
  fetching.value = false;
}

function showCreateContact() {
  refContactDialog.value!.reset();
  dialogUpdating.value = false;
  dialogContact.value = true;
}

function showUpdateContact(contact: ContactDto) {
  refContactDialog.value!.reset();
  refContactDialog.value!.load(contact);
  selectedContact.value = contact;
  dialogUpdating.value = true;
  dialogContact.value = true;
}

function showDeleteContact(contact: ContactDto) {
  selectedContact.value = contact;
  dialogDelete.value = true;
}

async function submitContact(contact: ContactDto, actions: ContactPostAction) {
  if (dialogUpdating.value)
    await update(contact, actions);
  else
    await create(contact, actions);
}

async function create(contact: ContactDto, actions: ContactPostAction) {
  try {
    loading.value = true;
    const savedContact = await createContact(contact);
    await handleActions(savedContact.id, actions);
    dialogContact.value = false;
    await fetchData();
  } catch (e) {
    showSnackbar('Ein Fehler ist aufgetreten');
  } finally {
    loading.value = false;
  }
}

async function update(contact: ContactDto, actions: ContactPostAction) {
  try {
    loading.value = true;
    await updateContact({
      // @ts-ignore: overwrite id
      id: selectedContact.value.id,
      ...contact
    });
    await handleActions(selectedContact.value.id, actions);
    dialogContact.value = false;
    await fetchData();
  } catch (e) {
    showSnackbar('Ein Fehler ist aufgetreten');
  } finally {
    loading.value = false;
  }
}

async function handleActions(contactId: number, actions: ContactPostAction) {
  if (actions.uploadingFile) {
    await setContactFile({contactId, file: actions.uploadingFile});
  } else if (actions.deletingFile) {
    await deleteContactFile({contactId});
  }
}

async function moveUp(contact: ContactDto) {
  try {
    loading.value = true;
    await moveUpContact({'id': contact.id});
    await fetchData();
  } catch (e) {
    showSnackbar('Ein Fehler ist aufgetreten');
  } finally {
    loading.value = false;
  }
}

async function moveDown(contact: ContactDto) {
  try {
    loading.value = true;
    await moveDownContact({id: contact.id});
    await fetchData();
  } catch (e) {
    showSnackbar('Ein Fehler ist aufgetreten');
  } finally {
    loading.value = false;
  }
}

async function deleteSelectedContact() {
  try {
    loading.value = true;
    await deleteContact({id: selectedContact.value.id});
    dialogDelete.value = false;
    await fetchData();
  } catch (e) {
    showSnackbar('Ein Fehler ist aufgetreten');
  } finally {
    loading.value = false;
  }
}

const localized = (obj: MultiLocaleString) => localizedString(obj, locale.value);

const place = (contact: ContactDto) => {
  return contact.place?.replaceAll('\n', '<br>');
}

const fileUrl = (file: string) => getFileUrl(file) + '#' + new Date().getTime();

onMounted(async () => {
  await fetchData();
});
</script>

<style>
.contact-info td {
  padding-bottom: 0.5rem;
  vertical-align: top;
}
</style>
