<template>
  <MainContainer title="Moderatoren" icon="mdi-account-multiple">

    <template v-slot:meta>
      <p class="text-h6">{{ users.length }} {{ users.length === 1 ? 'Moderator' : 'Moderatoren' }}</p>

      <v-btn
          color="primary"
          dark
          @click="dialogAdd = true"
      >
        <v-icon left>mdi-account-plus</v-icon>
        Neuer Nutzer
      </v-btn>

    </template>

    <v-simple-table>
      <thead>
        <tr>
          <th class="text-left">Name</th>
          <th class="text-left">Passwort</th>
          <th class="text-left">Aktionen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.name }}</td>
          <td>******</td>
          <td>
            <v-btn @click="openUpdateName(u)" elevation="2" color="primary">
              <v-icon>mdi-account-edit</v-icon>
            </v-btn>

            <v-btn @click="openUpdatePassword(u)" elevation="2" color="primary" class="ml-4">
              <v-icon>mdi-key</v-icon>
            </v-btn>

            <v-btn @click="openDeleteUser(u)" elevation="2" color="primary" class="ml-4">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </td>
        </tr>
      </tbody>
    </v-simple-table>

    <v-dialog v-model="dialogAdd" width="500">
      <v-card>
        <v-card-title class="headline">
          Neuer Nutzer
        </v-card-title>

        <v-card-text>
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
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="addUser" color="primary" :loading="loading">
            <v-icon left>mdi-account-plus</v-icon>
            Erstellen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogUpdateName" width="500">
      <v-card>
        <v-card-title class="headline">
          Nutzer umbenennen
        </v-card-title>

        <v-card-text>
          <v-text-field v-model="name" label="Name" prepend-icon="mdi-account" :disabled="loading"></v-text-field>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="updateName" color="primary" :loading="loading">
            <v-icon left>mdi-account-edit</v-icon>
            Umbenennen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogUpdatePassword" width="500">
      <v-card>
        <v-card-title class="headline">
          Passwort von {{ selectedUser.name }} ändern
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-col cols="6">
              <v-text-field v-model="password" label="Passwort" prepend-icon="mdi-key" type="password" :disabled="loading"></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="passwordRepeat" label="Passwort wiederholen" type="password" :disabled="loading"></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="updatePassword" color="primary" :loading="loading">
            <v-icon left>mdi-content-save</v-icon>
            Speichern
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDeleteUser" width="500">
      <v-card>
        <v-card-title class="headline">
          {{ selectedUser.name }} löschen
        </v-card-title>

        <v-card-text>
          Möchten Sie wirklich {{ selectedUser.name }} löschen?
          <br>
          Logdaten, die mit dem Nutzer zusammenhängen werden ebenfalls gelöscht. Öffentliche Inhalte werden <b>nicht</b> gelöscht.
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="resetAndCloseAll" color="black" text :disabled="loading">
            Abbrechen
          </v-btn>
          <v-btn @click="deleteUser" color="primary" :loading="loading">
            <v-icon left>mdi-delete</v-icon>
            Löschen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </MainContainer>
</template>

<script>
import {addUser, deleteUser, getUsers, updateUserName, updateUserPassword} from "@/api";
import MainContainer from "@/components/layout/MainContainer";
import {showSnackbar} from "@/utils";

export default {
  name: 'UsersView',
  components: {MainContainer},
  data: () => ({
    loading: false,
    users: [],
    selectedUser: {},
    dialogAdd: false,
    dialogUpdateName: false,
    dialogUpdatePassword: false,
    dialogDeleteUser: false,
    name: '',
    password: '',
    passwordRepeat: ''
  }),
  methods: {
    fetchData: async function() {
      this.users = (await getUsers()).data;
    },
    resetAndCloseAll: function() {
      this.name = '';
      this.password = '';
      this.passwordRepeat = '';
      this.dialogAdd = false;
      this.dialogUpdateName = false;
      this.dialogUpdatePassword = false;
      this.dialogDeleteUser = false;
    },
    openUpdateName: function(user) {
      this.selectedUser = user;
      this.dialogUpdateName = true;
      this.name = user.name;
    },
    openUpdatePassword: function(user) {
      this.selectedUser = user;
      this.dialogUpdatePassword = true;
    },
    openDeleteUser: function(user) {
      this.selectedUser = user;
      this.dialogDeleteUser = true;
    },
    addUser: async function() {

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
        await addUser({ name: this.name, password: this.password});
        await this.fetchData();
        this.resetAndCloseAll();
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
        await this.fetchData();
        this.resetAndCloseAll();
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
        await this.fetchData();
        this.resetAndCloseAll();
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
        await this.fetchData();
        this.resetAndCloseAll();
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