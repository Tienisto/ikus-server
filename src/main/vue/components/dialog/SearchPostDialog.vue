<template>
  <GenericSearchDialog ref="searchDialog" v-model="value" dialog-title="Beitrag auswählen"
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
import GenericSearchDialog from "@/components/dialog/GenericSearchDialog";
import {localizedString} from "~/utils/localization";

export default {
  name: 'SearchPostDialog',
  components: {GenericSearchDialog},
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    locale: {
      type: String,
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
    value: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      }
    },
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    },
    timeString: function() {
      return (time) => moment(time).format('ddd, DD.MM.YYYY');
    }
  }
}
</script>
