<template>
  <GenericSearchDialog ref="searchDialog" :value="value" @input="$emit('input', $event)" dialog-title="Beitrag auswählen"
                       :search-func="searchFunc" @select="$emit('select', $event)">
    <template v-slot="{ item }">
      <div>
        <span class="font-weight-bold">{{ localized(item.title) }}</span>
        <br>
        <span>{{ timeString(item.date) }} / {{ localized(item.channel.name) }}</span>
      </div>
    </template>
  </GenericSearchDialog>
</template>

<script>
import moment from "moment";
import {searchPosts} from "@/api";
import {localizedString} from "@/utils";
import GenericSearchDialog from "@/components/dialog/GenericSearchDialog";

export default {
  name: 'SearchPostDialog',
  components: {GenericSearchDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    }
  },
  data: () => ({
    searchFunc: searchPosts
  }),
  methods: {
    reset: function() {
      // reset input
      this.$refs.searchDialog.reset();
    }
  },
  computed: {
    localized: function() {
      return (obj) => localizedString(obj, 'EN');
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    }
  }
}
</script>