<template>
  <v-card>
    <v-card-text class="pt-6 pb-6" style="text-align: center">
      <span class="text-h3">{{ actualNumber }}</span>
      <br>
      <span class="text-h5">{{ label }}</span>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
export default {
  name: 'NumberCard',
  props: {
    label: {
      type: String,
      required: true
    },
    number: {
      type: Number,
      required: true
    },
    animationDuration: {
      // in millis
      type: Number,
      required: true
    }
  },
  data: () => ({
    initialized: false,
    actualNumber: 0,
    animation: {
      from: 0,
      delta: 0,
      startTime: 0
    },
    timerId: null as number | null
  }),
  methods: {
    tick: function() {
      const percentage = (new Date().getTime() - this.animation.startTime) / this.animationDuration;

      if (percentage >= 1) {
        this.actualNumber = this.animation.from + this.animation.delta;
      } else {
        this.actualNumber = Math.round(this.animation.from + this.animation.delta * percentage);
      }
    }
  },
  watch: {
    number: {
      immediate: true,
      handler: function(newVal, oldVal) {
        if (!this.initialized && !oldVal) {
          // set first value immediately
          this.actualNumber = newVal;
          return;
        }

        if (this.timerId) {
          // stop old animation
          clearInterval(this.timerId);
        }

        this.animation = {
          from: oldVal,
          delta: newVal - oldVal,
          startTime: new Date().getTime()
        };

        this.initialized = true;
        this.timerId = setInterval(this.tick, 20);
      }
    }
  }
}
</script>

