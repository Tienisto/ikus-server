<template>
  <v-card>
    <v-card-title>
      <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
        <span>
          {{ title }}
          <v-btn @click="$emit('move-up')" class="black--text ml-2" icon small>
            <v-icon>mdi-arrow-up</v-icon>
          </v-btn>
          <v-btn @click="$emit('move-down')" class="black--text ml-2" icon small>
            <v-icon>mdi-arrow-down</v-icon>
          </v-btn>
          <v-btn @click="$emit('edit')" class="black--text ml-2" icon small>
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn @click="$emit('delete')" class="black--text ml-2" icon small>
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </span>
        <v-btn @click="$emit('create')" color="primary" small>
          <v-icon>mdi-plus</v-icon>
        </v-btn>
      </div>
    </v-card-title>
    <v-card-text>
      <v-card outlined>
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

<script>
import Notice from "@/components/Notice";

export default {
  name: 'ListCard',
  components: {Notice},
  props: {
    title: {
      type: String,
      required: true
    },
    items: {
      type: Array,
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