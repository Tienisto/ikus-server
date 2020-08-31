<template>
  <v-card class="ml-10" color="secondary">
    <v-card-text>
      <div style="display: flex; align-items: flex-start">
        <div style="flex: 1">
          <v-simple-table class="secondary">
            <thead>
            <tr>
              <th class="text-left">Name (englisch)</th>
              <th class="text-left">Name (deutsch)</th>
              <th class="text-left">Aktionen</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="c in channels" :key="c.id">
              <td>{{ c.name.en }}</td>
              <td>{{ c.name.de }}</td>
              <td>
                <v-btn @click="$emit('update', c)" elevation="2" color="primary">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>

                <v-btn @click="$emit('delete', c)" class="ml-4" elevation="2" color="primary">
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </td>
            </tr>
            </tbody>
          </v-simple-table>
        </div>

        <v-btn @click="$emit('create')" class="ml-10 mt-2" color="primary" dark large>
          <v-icon left>mdi-plus</v-icon>
          Neuer Kanal
        </v-btn>
      </div>
    </v-card-text>
  </v-card>

</template>

<script>
export default {
  name: 'ChannelTabItem',
  props: ['channels'],
  data: () => ({

  })
}
</script>