<template>
  <div class="primary" style="height: 100vh; width: 100%; display: flex; align-items: center; justify-content: center">
    <div style="text-align: center">
      <img src="@/assets/logo-512-alpha.png" style="width: 200px">
      <p class="white--text text-h4">IKUS - App</p>
      <p class="white--text text-h5">Administration</p>
      <v-card class="mt-10">
        <v-card-text>
          <v-text-field v-model="name" @keydown.enter="login" prepend-icon="mdi-account" type="text" placeholder="Name" :disabled="fetching" />
          <v-text-field v-model="password" @keydown.enter="login" prepend-icon="mdi-key" type="password" placeholder="Passwort" :disabled="fetching" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="login" color="success" :disabled="fetching">
            Login
            <v-icon right>mdi-arrow-right</v-icon>
          </v-btn>
        </v-card-actions>
      </v-card>
    </div>

    <v-snackbar v-model="showError" :bottom="true" :right="true">
      Login fehlgeschlagen

      <template v-slot:action="{}">
        <v-btn text @click="showError = false">
          Schließen
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
import { login } from '@/api'

export default {
  name: 'Login',
  data: () => ({
    fetching: false,
    name: null,
    password: null,
    showError: false
  }),
  methods: {
    login: async function() {
      this.fetching = true;
      try {
        await login({ name: this.name, password: this.password });
        await this.$emit('update-login');
        await this.$router.push('/dashboard');
      } catch(status) {
        this.showError = true;
      } finally {
        this.fetching = false;
      }
    }
  }
}
</script>
