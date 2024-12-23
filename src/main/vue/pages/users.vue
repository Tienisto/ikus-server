<template>
  <MainContainer title="Moderatoren" icon="mdi-account-multiple">

    <template v-slot:intro>
      Alle Moderatoren haben (bis jetzt) im System dieselben Rechte.
      <br>
      Die Passwörter sind verschlüsselt (hashed).
    </template>

    <template v-slot:meta>
      <p class="text-h6 mb-3">{{ users.length }} {{ users.length === 1 ? 'Moderator' : 'Moderatoren' }}</p>

      <v-btn @click="showCreateDialog" color="primary" prepend-icon="mdi-account-plus">
        Neuer Nutzer
      </v-btn>

    </template>

    <v-card>
      <v-card-text>
        <v-data-table
            :loading="fetching"
            :headers="[
                { title: 'Name', key: 'name', value: item => item.name },
                { title: 'Passwort', value: 'password' },
                { title: 'Aktionen', value: 'actions' }
            ]"
            :items="users"
            :items-per-page="-1"
            loading-text="Lade Daten..."
            no-data-text="Keine Moderatoren vorhanden"
            hide-default-footer
        >
          <template v-slot:item.password="">
            ******
          </template>
          <template v-slot:item.actions="props">
            <v-btn @click="showUpdateNameDialog(props.item)" elevation="2" color="primary">
              <v-icon>mdi-account-edit</v-icon>
            </v-btn>

            <v-btn @click="showUpdatePasswordDialog(props.item)" elevation="2" color="primary" class="ml-4">
              <v-icon>mdi-key</v-icon>
            </v-btn>

            <v-btn @click="showDeleteUserDialog(props.item)" elevation="2" color="primary" class="ml-4">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <GenericDialog v-model="dialogCreateUser" title="Neuer Nutzer">
      <template v-slot:content>
        <v-row>
          <v-col cols="12">
            <v-text-field v-model="name" label="Name" prepend-icon="mdi-account" :disabled="loading"></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-text-field v-model="password" label="Passwort" prepend-icon="mdi-key" type="password" :disabled="loading"></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-text-field v-model="passwordRepeat" label="Passwort wiederholen" type="password" :disabled="loading"></v-text-field>
          </v-col>
        </v-row>
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogCreateUser = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="createUser" color="primary" prepend-icon="mdi-account-plus" :loading="loading" variant="elevated">
          Erstellen
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogUpdateName" title="Nutzer umbenennen">
      <template v-slot:content>
        <v-text-field v-model="name" label="Name" prepend-icon="mdi-account" :disabled="loading"></v-text-field>
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogUpdateName = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="updateName" color="primary" prepend-icon="mdi-account-edit" :loading="loading" variant="elevated">
          Umbenennen
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogUpdatePassword" :title="'Passwort von ' + selectedUser.name + ' ändern'">
      <template v-slot:content>
        <v-row>
          <v-col cols="6">
            <v-text-field v-model="password" label="Passwort" prepend-icon="mdi-key" type="password" :disabled="loading"></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-text-field v-model="passwordRepeat" label="Passwort wiederholen" type="password" :disabled="loading"></v-text-field>
          </v-col>
        </v-row>
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogUpdatePassword = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="updatePassword" color="primary" prepend-icon="mdi-content-save" :loading="loading" variant="elevated">
          Speichern
        </v-btn>
      </template>
    </GenericDialog>

    <GenericDialog v-model="dialogDeleteUser" :title="selectedUser.name + ' löschen'">
      <template v-slot:content>
        Möchten Sie wirklich {{ selectedUser.name }} löschen?
        <br>
        Logdaten, die mit dem Nutzer zusammenhängen, werden ebenfalls gelöscht. Öffentliche Inhalte werden <b>nicht</b> gelöscht.
      </template>

      <template v-slot:actions>
        <v-btn @click="dialogDeleteUser = false" color="black" :disabled="loading">
          Abbrechen
        </v-btn>
        <v-btn @click="deleteUser" color="primary" prepend-icon="mdi-delete" :loading="loading" variant="elevated">
          Löschen
        </v-btn>
      </template>
    </GenericDialog>

  </MainContainer>
</template>

<script lang="ts">
import {createUser, deleteUser, getUsers, updateUserName, updateUserPassword} from "@/api";
import MainContainer from "@/components/layout/MainContainer.vue";
import GenericDialog from "@/components/dialog/GenericDialog.vue";
import type {UserDto} from "@/models";
import {showSnackbar} from "@/utils/snackbar";

export default {
  name: 'UsersView',
  components: {GenericDialog, MainContainer},
  data: () => ({
    fetching: true,
    loading: false,
    users: [] as UserDto[],
    selectedUser: {} as UserDto,
    dialogCreateUser: false,
    dialogUpdateName: false,
    dialogUpdatePassword: false,
    dialogDeleteUser: false,
    name: '',
    password: '',
    passwordRepeat: ''
  }),
  methods: {
    fetchData: async function() {
      this.fetching = true;
      this.users = await getUsers();
      this.fetching = false;
    },
    resetDialogData: function() {
      this.name = '';
      this.password = '';
      this.passwordRepeat = '';
    },
    showCreateDialog() {
      this.resetDialogData();
      this.dialogCreateUser = true;
    },
    showUpdateNameDialog: function(user: UserDto) {
      this.resetDialogData();
      this.selectedUser = user;
      this.dialogUpdateName = true;
      this.name = user.name;
    },
    showUpdatePasswordDialog: function(user: UserDto) {
      this.resetDialogData();
      this.selectedUser = user;
      this.dialogUpdatePassword = true;
    },
    showDeleteUserDialog: function(user: UserDto) {
      this.resetDialogData();
      this.selectedUser = user;
      this.dialogDeleteUser = true;
    },
    createUser: async function() {

      if (!this.name || !this.password || !this.passwordRepeat) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      if (this.password !== this.passwordRepeat) {
        showSnackbar('Passwörter stimmen nicht überein');
        return;
      }

      try {
        this.loading = true;
        await createUser({ name: this.name, password: this.password});
        this.dialogCreateUser = false;
        await this.fetchData();
      } catch (e) {
        if (e === 409)
          showSnackbar('Name bereits vergeben');
        else
          showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updateName: async function() {

      if (!this.name) {
        showSnackbar('Bitte Name eingeben');
        return;
      }

      try {
        this.loading = true;
        await updateUserName({ id: this.selectedUser.id, name: this.name });
        this.dialogUpdateName = false;
        await this.fetchData();
      } catch (e) {
        if (e === 409)
          showSnackbar('Name bereits vergeben');
        else
          showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    updatePassword: async function() {

      if (!this.password || !this.passwordRepeat) {
        showSnackbar('Bitte alle Felder ausfüllen');
        return;
      }

      if (this.password !== this.passwordRepeat) {
        showSnackbar('Passwörter stimmen nicht überein');
        return;
      }

      try {
        this.loading = true;
        await updateUserPassword({ id: this.selectedUser.id, password: this.password });
        this.dialogUpdatePassword = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    deleteUser: async function() {
      try {
        this.loading = true;
        await deleteUser({id: this.selectedUser.id});
        this.dialogDeleteUser = false;
        await this.fetchData();
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    }
  },
  mounted: async function() {
    await this.fetchData();
  }
}
</script>
