<template>
  <v-card>
    <v-card-title>
      <div style="width: 100%; display: flex; align-items: center; justify-content: space-between">
        <span>
          {{ localized(audio.name) }}
          <v-btn @click="$emit('move-up-group')" :disabled="loading" class="black--text ml-2" icon small>
            <v-icon>mdi-arrow-up</v-icon>
          </v-btn>
          <v-btn @click="$emit('move-down-group')" :disabled="loading" class="black--text ml-2" icon small>
            <v-icon>mdi-arrow-down</v-icon>
          </v-btn>
          <v-btn @click="$emit('update-group')" :disabled="loading" class="black--text ml-2" icon small>
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn @click="$emit('delete-group')" :disabled="loading" class="black--text ml-2" icon small>
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </span>
        <span>
          <v-btn @click="$emit('create-file')" :disabled="loading" color="primary" small>
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </span>
      </div>
    </v-card-title>
    <v-card-text>

      <v-row no-gutters>
        <v-col cols="8">
          <div style="text-align: right">

          </div>
          <v-card class="mt-4" outlined>
            <v-card-text class="pa-0">
              <div style="height: 300px; overflow-y: scroll" >

                <div v-if="audio.files.length === 0" class="pl-2 pt-2">
                  <Notice title="Keine Audio-Dateien" />
                </div>

                <div v-for="f in audio.files" :key="f.id" @mouseover="mouseOver = f.id" @mouseleave="mouseOver = -1">
                  <div class="ml-2 mt-2 mb-2" style="display: flex; align-items: center">
                    <div style="flex: 1">
                      {{ localized(f.name) }}
                    </div>
                    <div style="display: flex" class="pl-1 pr-1" :style="mouseOver === f.id ? {} : { 'visibility': 'hidden' }">
                      <v-btn @click="$emit('move-up-file', audio, f)" :disabled="loading" icon small>
                        <v-icon>mdi-arrow-up</v-icon>
                      </v-btn>
                      <v-btn @click="$emit('move-down-file', audio, f)" :disabled="loading" class="ml-2" icon small>
                        <v-icon>mdi-arrow-down</v-icon>
                      </v-btn>
                      <v-btn @click="$emit('update-file', audio, f)" :disabled="loading" class="ml-2" icon small>
                        <v-icon>mdi-pencil</v-icon>
                      </v-btn>
                      <v-btn @click="$emit('delete-file', audio, f)" :disabled="loading" class="ml-2" icon small>
                        <v-icon>mdi-delete</v-icon>
                      </v-btn>
                    </div>
                  </div>
                  <v-divider />
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="4" class="pa-4">
          <!-- using v-img causes weird resize effects -->
          <img v-if="audioImage" :src="audioImage" style="width: 100%" />
        </v-col>
      </v-row>

    </v-card-text>
  </v-card>
</template>

<script>
import Notice from "@/components/Notice";
import {localizedString} from "@/utils";
import {getFileUrl} from "@/api";

export default {
  name: 'AudioCard',
  components: {Notice},
  props: {
    audio: {
      type: Object,
      required: true
    },
    locale: {
      type: String,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  },
  data: () => ({
    mouseOver: -1,
    audioImage: null,
  }),
  methods: {
    updateAudioImage: function(newVal) {
      if (newVal.image) {
        this.audioImage = this.fileUrl(this.localized(newVal.image));
      }
    }
  },
  computed: {
    localized: function() {
      return (obj) => localizedString(obj, this.locale);
    },
    fileUrl: function() {
      return (file) => getFileUrl(file) + '#' + new Date().getTime();
    }
  },
  watch: {
    audio: function(newVal) {
      this.updateAudioImage(newVal);
    },
    locale: function() {
      this.updateAudioImage(this.audio)
    }
  },
  mounted: function() {
    this.updateAudioImage(this.audio);
  }
}
</script>