<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" title="Icon auswählen" :width="550">
    <template v-slot:content>

      <span>
        Alle Icons finden Sie auch unter
        <br>
        <a href="https://material.io/resources/icons/?style=baseline" target="_blank">https://material.io/resources/icons/?style=baseline</a>
        <br>
        Lizensiert unter <a href="https://www.apache.org/licenses/LICENSE-2.0.html" target="_blank">Apache License, Version 2.0</a>
      </span>

      <v-text-field class="mt-4" v-model="query" label="Suchen" :disabled="loading" filled hide-details
                    @keydown.enter="search" @click:append="search" append-icon="mdi-magnify"></v-text-field>

      <br>

      <Notice v-if="icons.length === 0" class="mt-6 mb-6" title="Keine Ergebnisse." />

      <v-virtual-scroll :items="icons" height="350" item-height="100">
        <template v-slot="{ item }">
          <div style="display: flex;">
            <div style="text-align: center; width: 110px;" v-for="(icon, index) in item" :key="index">
              <i @click="$emit('select', icon)" class="material-icons" style="cursor: pointer; font-size: 40px;">{{ icon }}</i>
              <div @click="$emit('select', icon)" style="cursor: pointer;">{{ icon }}</div>
            </div>
          </div>
        </template>
      </v-virtual-scroll>
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text :disabled="loading">
        Schließen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import allIcons from "@/material-icons.json";
import {showSnackbar} from "@/utils";
import GenericDialog from "@/components/dialog/GenericDialog";
import Notice from "@/components/Notice";

export default {
  name: 'IconChooserDialog',
  components: {Notice, GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
  },
  data: () => ({
    loading: false,
    query: '',
    icons: []
  }),
  methods: {
    reset: function() {
      // reset input
      this.query = '';
      this.icons = this.groupN(allIcons);
    },
    search: async function() {
      try {
        this.loading = true;
        const query = this.query.toLowerCase();
        this.icons = this.groupN(allIcons.filter(name => name.includes(query)));
      } catch (e) {
        showSnackbar('Ein Fehler ist aufgetreten');
      } finally {
        this.loading = false;
      }
    },
    groupN: function(items) {

      // create sub lists of 4 items
      // e.g. [1, 2, 3, 4, 5, 6] -> [ [1, 2, 3, 4], [5, 6] ]

      const result = [];
      for(let i = 0; i + 3 < items.length; i += 4) {
        result.push([items[i], items[i+1], items[i+2], items[i+3]])
      }

      if (items.length % 4 !== 0) {
        const lastRow = [];
        for(let i = items.length - items.length % 4; i < items.length; i++) {
          lastRow.push(items[i]);
        }
        result.push(lastRow);
      }

      return result;
    }
  }
}
</script>