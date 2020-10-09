<template>
  <v-dialog ref="dialog" v-model="dialog" width="570px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="valueFormatted" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly
                    :append-icon="valueExisting ? 'mdi-delete' : null"
                    @click:append="reset" filled></v-text-field>
    </template>

    <v-card>

      <v-card-title>Koordinaten</v-card-title>
      <v-card-subtitle>{{ valueFormatted }}</v-card-subtitle>

      <v-card-text>
        <vl-map :load-tiles-while-animating="true" :load-tiles-while-interacting="true" data-projection="EPSG:4326" style="height: 400px">
          <vl-view :zoom.sync="zoom" :center="valueToCenter" @update:center="$emit('input', centerToValue($event))"></vl-view>

          <vl-layer-tile id="osm">
            <vl-source-osm></vl-source-osm>
          </vl-layer-tile>
        </vl-map>
        <div style="position: absolute; left: 50%; top: 50%; transform: translate(-50%, -100%)">
          <v-icon color="primary" style="font-size: 3rem">mdi-map-marker</v-icon>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text color="primary" @click="dialog = false">Schließen</v-btn>
      </v-card-actions>
    </v-card>

  </v-dialog>
</template>

<script>

export default {
  name: 'LocationPicker',
  props: {
    value: {
      type: Object,
      required: true
    },
    label: {
      type: String,
      required: true
    },
    icon: {
      type: String
    }
  },
  data: () => ({
    dialog: false,
    zoom: 16,
    prevCenter: [11.64531, 52.13960]
  }),
  methods: {
    reset: function() {
      this.$emit('input', {});
    }
  },
  computed: {
    valueExisting: function() {
      return this.value && this.value.x && this.value.y;
    },
    valueToCenter: function() {
      if (this.valueExisting)
        return [this.value.y, this.value.x];
      else
        return this.prevCenter;
    },
    centerToValue: function() {
      return (center) => ({
        x: center[1],
        y: center[0]
      })
    },
    valueFormatted: function() {
      if (this.valueExisting)
        return '('+ this.value.x.toFixed(5) + ', ' + this.value.y.toFixed(5) + ')';
      else
        return '-';
    }
  },
  watch: {
    value: function() {
      this.prevCenter = this.valueToCenter;
    }
  }
}
</script>