<template>
  <div style="display: flex; align-items: flex-start" class="pl-10">
    <div style="flex: 1">
      <v-data-table
          :loading="loading"
          :headers="[
                { text: 'Name (englisch)', value: 'name.en' },
                { text: 'Name (deutsch)', value: 'name.de' },
                { text: 'Aktionen', value: 'actions' }
            ]"
          :items="channels"
          loading-text="Lade Daten..."
          no-data-text="Keine Kanäle vorhanden"
          hide-default-footer
      >

        <template v-slot:item.actions="props">
          <v-btn @click="$emit('update', props.item)" elevation="2" color="primary">
            <v-icon>mdi-pencil</v-icon>
          </v-btn>

          <v-btn @click="$emit('delete', props.item)" class="ml-4" elevation="2" color="primary">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </div>

    <v-btn @click="$emit('create')" class="ml-10 mt-2" color="primary" dark large>
      <v-icon left>mdi-plus</v-icon>
      Neuer Kanal
    </v-btn>
  </div>
</template>

<script>
export default {
  name: 'ChannelTabItem',
  props: ['channels', 'loading'],
  data: () => ({

  })
}
</script>