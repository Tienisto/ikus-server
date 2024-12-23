<template>
  <GenericSearchDialog ref="searchDialog" v-model="value" dialog-title="Link auswählen"
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

<script lang="ts">
import {searchLinks} from "@/api";
import GenericSearchDialog from "@/components/dialog/GenericSearchDialog";
import {localizedString} from "~/utils/localization";

export default {
  name: 'SearchLinkDialog',
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
    searchFunc: searchLinks
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
    }
  }
}
</script>
