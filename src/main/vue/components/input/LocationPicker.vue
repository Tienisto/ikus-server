<template>
  <div>
    <v-text-field @click="openPicker"
                  :model-value="valueFormatted(modelValue)"
                  :label="label"
                  :prepend-icon="icon"
                  :append-icon="modelValue ? 'mdi-delete' : undefined"
                  hide-details readonly filled
                  @click:append="reset" />
    <v-dialog v-model="dialog" width="550px">
      <v-card>

        <v-card-title>Koordinaten</v-card-title>
        <v-card-subtitle>{{ internalValueFormatted(internalValue) }}</v-card-subtitle>

        <v-card-text>
          <div style="height:400px; width:500px">
            <l-map ref="map" v-model:zoom="zoom" :center="internalValue!" @update:center="updateCenter" :use-global-leaflet="false">
              <l-tile-layer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  layer-type="base"
                  name="OpenStreetMap"
              ></l-tile-layer>
            </l-map>
          </div>
          <div style="position: absolute; left: 50%; top: 50%; transform: translate(-50%, -100%); z-index: 999">
            <v-icon color="primary" style="font-size: 3rem">mdi-map-marker</v-icon>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="dialog = false">
            Abbrechen
          </v-btn>
          <v-btn @click="save" color="primary" prepend-icon="mdi-content-save" variant="elevated">
            Speichern
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import "leaflet/dist/leaflet.css"
import "leaflet/dist/leaflet.css";
import {LMap, LTileLayer} from "@vue-leaflet/vue-leaflet";

const defaultLocation = { x: 52.13960, y: 11.64531 };

const props = defineProps<{
  modelValue: { x: number, y: number } | null,
  label: string,
  icon: string
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', time: { x: number, y: number } | null): void
}>();

const dialog = ref(false);
const internalValue = ref<[number, number] | null>(null);
const zoom = ref(16);

const openPicker = () => {
  internalValue.value = valueToVirtual(props.modelValue ?? defaultLocation);
  dialog.value = true;
};

const reset = () => {
  emit('update:modelValue', null);
};

const save = () => {
  if (internalValue.value === null) {
    return;
  }
  emit('update:modelValue', virtualToValue(internalValue.value));
  dialog.value = false;
};

const valueToVirtual = (value: { x: number, y: number }): [number, number] => {
  return [value.x, value.y];
};

const virtualToValue = (center: [number, number]) => ({
  x: center[0],
  y: center[1]
});

const updateCenter = (center: {lat: number, lng: number}) => {
  // https://github.com/vue-leaflet/vue-leaflet/issues/305
  internalValue.value = [center.lat, center.lng];
};

const valueFormatted = (value: { x: number, y: number } | null) => {
  if (!value) {
    return null;
  }
  return '('+ value.x.toFixed(5) + ', ' + value.y.toFixed(5) + ')';
};

const internalValueFormatted = (value: [number, number] | null) => {
  if (!value) {
    return null;
  }
  return valueFormatted(virtualToValue(value));
};
</script>
