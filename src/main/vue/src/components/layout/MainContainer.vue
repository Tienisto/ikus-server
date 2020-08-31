<template>
  <div style="margin-top: 50px; padding: 20px;">
    <div style="margin: auto; max-width: 1100px">

      <!-- title -->
      <div class="mb-8">
        <v-icon size="26" class="primary--text">{{ icon }}</v-icon>
        <span class="ml-2 primary--text">{{ title }}</span>
      </div>

      <div style="margin: auto; display: flex; align-items: flex-start" class="text-h5">

        <!-- body -->
        <div style="flex: 1">
          <slot></slot>
        </div>

        <!-- meta -->
        <v-card class="ml-12" v-if="$slots.meta">
          <v-card-text class="secondary">
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