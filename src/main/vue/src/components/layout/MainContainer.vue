<template>
  <div id="main-container-wrapper" class="secondary" style="padding: 30px 30px 100px 30px; min-height: 100vh">

    <div v-if="topSpacer" id="main-container-top-spacer" style="display: none; height: 30px"></div>

    <div style="margin: auto" :style="{ 'max-width': maxWidth+'px' }">
      <!-- title -->
      <div class="mb-8 ml-4 ml-sm-0">
        <v-icon size="26" class="primary--text">{{ icon }}</v-icon>
        <span class="ml-2 primary--text">{{ title }}</span>
      </div>

      <div class="subtitle-1 mb-8" v-if="$slots.intro">
        <slot name="intro"></slot>
      </div>

      <div style="margin: auto; display: flex; align-items: flex-start">

        <!-- body -->
        <div style="flex: 1">
          <slot></slot>
        </div>

        <!-- meta -->
        <v-card class="ml-12" v-if="$slots.meta">
          <v-card-text>
            <slot name="meta"></slot>
          </v-card-text>
        </v-card>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'MainContainer',
  props: {
    title: {
      type: String,
      required: true
    },
    icon: {
      type: String,
      required: true
    },
    maxWidth: {
      type: Number,
      default: 1200
    },
    topSpacer: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style>
@media screen and (min-width: 1500px) {
  #main-container-top-spacer {
    display: block !important;
  }
}

@media screen and (max-width: 600px) {
  #main-container-wrapper {
    padding: 30px 0 100px 0 !important;
  }
}
</style>