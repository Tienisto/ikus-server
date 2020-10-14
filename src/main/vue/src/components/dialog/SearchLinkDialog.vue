<template>
  <GenericSearchDialog ref="searchDialog" :value="value" @input="$emit('input', $event)" dialog-title="Link auswählen"
                       :search-func="searchFunc" @select="$emit('select', $event)">
    <template v-slot="{ item }">
      <div>
        <span class="font-weight-bold">{{ localized(item.info) }}</span>
        <br>
        <span>{{ localized(item.url) }}</span>
      </div>
    </template>
  </GenericSearchDialog>
</template>

<script>
import {searchLinks} from "@/api";
import {localizedString} from "@/utils";
import GenericSearchDialog from "@/components/dialog/GenericSearchDialog";

export default {
  name: 'SearchLinkDialog',
  components: {GenericSearchDialog},
  props: {
    value: {
      type: Boolean,
      required: true
    },
    locale: {
      type: String,
      required: true
    }
  },
  data: () => ({
    searchFunc: searchLinks
  }),
  methods: {
    reset: function() {
      // reset input
      this.$refs.searchDialog.reset();
    }
  },
  computed: {
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    }
  }
}
</script>