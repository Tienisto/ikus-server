<template>
  <div class="secondary" style="padding: 30px 30px 100px 30px; min-height: 100vh">
    <div id="main-container-top-spacer" style="display: none; height: 30px"></div>
    <div style="margin: auto; max-width: 1100px">

      <!-- title -->
      <div class="mb-8">
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
  props: ['title', 'icon']
}
</script>

<style>
@media screen and (min-width: 1500px) {
  #main-container-top-spacer {
    display: block !important;
  }
}
</style>