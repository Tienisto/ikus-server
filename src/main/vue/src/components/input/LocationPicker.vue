<template>
  <v-dialog ref="dialog" v-model="dialog" width="550px">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field :value="valueFormatted(value)" :label="label" :prepend-icon="icon" v-bind="attrs" v-on="on" hide-details readonly
                    :append-icon="value ? 'mdi-delete' : null"
                    @click:append="reset" filled></v-text-field>
    </template>

    <v-card>

      <v-card-title>Koordinaten</v-card-title>
      <v-card-subtitle>{{ valueFormatted(currValue) }}</v-card-subtitle>

      <v-card-text>
        <vl-map :load-tiles-while-animating="true" :load-tiles-while-interacting="true" data-projection="EPSG:4326" style="height: 400px">
          <vl-view :zoom.sync="zoom" :center="valueToVirtual" @update:center="currValue = virtualToValue($event)"></vl-view>

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
        <v-btn @click="dialog = false" color="black" text>
          Abbrechen
        </v-btn>
        <v-btn @click="save" color="primary">
          <v-icon left>mdi-content-save</v-icon>
          Speichern
        </v-btn>
      </v-card-actions>
    </v-card>

  </v-dialog>
</template>

<script>

const defaultLocation = { x: 52.13960, y: 11.64531 };

export default {
  name: 'LocationPicker',
  props: {
    value: {
      required: true,
      validator: v => typeof v === 'object' || v === null
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
    currValue: defaultLocation,
    zoom: 16
  }),
  methods: {
    reset: function() {
      this.$emit('input', null);
    },
    save: function() {
      this.$emit('input', this.currValue);
      this.dialog = false;
    }
  },
  computed: {
    valueToVirtual: function() {
      return [this.currValue.y, this.currValue.x];
    },
    virtualToValue: function() {
      return (center) => ({
        x: center[1],
        y: center[0]
      })
    },
    valueFormatted: function() {
      return (value) => {
        if (value)
          return '('+ value.x.toFixed(5) + ', ' + value.y.toFixed(5) + ')';
        else
          return null;
      };
    }
  },
  watch: {
    dialog: function(newVal) {
      if (newVal) {
        if (!this.value) {
          this.currValue = defaultLocation;
        } else {
          this.currValue = this.value;
        }
      }
    },
  },
}
</script>