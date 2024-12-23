<template>
  <v-card>
    <v-card-title>
      <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
        <span>
          {{ title }}
          <v-btn @click="$emit('move-up')" icon="mdi-arrow-up"
                 :disabled="loading" class="ml-2" variant="text" size="small" />
          <v-btn @click="$emit('move-down')" icon="mdi-arrow-down"
                 :disabled="loading" variant="text" size="small" />
          <v-btn @click="$emit('edit')" icon="mdi-pencil"
                 :disabled="loading" variant="text" size="small" />
          <v-btn @click="$emit('delete')" icon="mdi-delete"
                 :disabled="loading" variant="text" size="small" />
        </span>
        <v-btn @click="$emit('create')" icon="mdi-plus"
               :disabled="loading" color="primary" size="small" />
      </div>
    </v-card-title>
    <v-card-text>
      <v-card variant="text" style="border: solid 1px lightgrey">
        <v-card-text class="pa-0">
          <div style="height: 250px; overflow-y: scroll" >

            <div v-if="emptyNotice" class="pl-2 pt-2">
              <Notice :title="emptyNoticeText" />
            </div>

            <div v-for="item in items" :key="item.id" @mouseover="mouseOver = item.id" @mouseleave="mouseOver = -1">
              <div class="ml-2 mt-2 mb-2" style="display: flex; align-items: center">
                <div style="flex: 1">
                  <slot name="item" :item="item"></slot>
                </div>
                <div style="display: flex" class="pl-1 pr-1" :style="mouseOver === item.id ? {} : { 'visibility': 'hidden' }">
                  <slot name="actions" :item="item"></slot>
                </div>
              </div>
              <v-divider />
            </div>
          </div>
        </v-card-text>
      </v-card>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Notice from "@/components/Notice.vue";
import type {PropType} from "vue";
import type {IdItem} from "~/models";

export default {
  name: 'ListCard',
  components: {Notice},
  props: {
    title: {
      type: String,
      required: true
    },
    items: {
      type: Array as PropType<IdItem[]>,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    },
    emptyNotice: {
      type: Boolean,
      required: true
    },
    emptyNoticeText: {
      type: String,
      required: true
    }
  },
  data: () => ({
    mouseOver: -1
  })
}
</script>
