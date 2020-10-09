<template>
  <GenericDialog :value="value" @input="$emit('input', $event)" :title="title">
    <template v-slot:content>

      <p>{{ info }}</p>

      <div v-for="p in posts" :key="p.id" class="mt-4" style="display: flex; align-items: center; justify-content: space-between">
        <div>
          <b>{{ p.channel.name.en }}</b>
          <br>
          {{ p.title.en }}
        </div>
        <v-btn @click="$emit('delete', p)" icon :disabled="loading">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>

      <Notice v-if="posts.length === 0" title="Leer" class="ml-6 mt-6 mr-6" />
    </template>

    <template v-slot:actions>
      <v-btn @click="$emit('input', false)" color="black" text>
        Schließen
      </v-btn>
    </template>
  </GenericDialog>
</template>

<script>
import GenericDialog from "@/components/dialog/GenericDialog";
import Notice from "@/components/Notice";

export default {
  name: 'PostListDialog',
  components: {Notice, GenericDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    info: {
      type: String,
      default: ''
    },
    posts: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>