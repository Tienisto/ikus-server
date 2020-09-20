<template>
  <div class="rich-editor" style="min-height: 400px;">
    <editor-menu-bar :editor="editor" v-slot="{ commands, isActive, getMarkAttrs }">
      <div>
        <ToolbarButton tooltip="Rückgängig" @click="commands.undo">
          <v-icon>mdi-undo</v-icon>
        </ToolbarButton>
        <ToolbarButton tooltip="Wiederherstellen" @click="commands.redo">
          <v-icon>mdi-redo</v-icon>
        </ToolbarButton>
        <v-divider vertical class="black"/>
        <ToolbarButton tooltip="Fett" @click="commands.bold" :active="isActive.bold()">
          <v-icon>mdi-format-bold</v-icon>
        </ToolbarButton>
        <ToolbarButton tooltip="Kursiv" @click="commands.italic" :active="isActive.italic()">
          <v-icon>mdi-format-italic</v-icon>
        </ToolbarButton>
        <ToolbarButton tooltip="Unterstrichen" @click="commands.underline" :active="isActive.underline()">
          <v-icon>mdi-format-underline</v-icon>
        </ToolbarButton>
        <v-divider vertical class="black"/>
        <ToolbarButton tooltip="Überschrift 1" @click="commands.heading({ level: 2 })" :active="isActive.heading({ level: 2 })">
          <span class="text-body-1 font-weight-bold">Ü1</span>
        </ToolbarButton>
        <ToolbarButton tooltip="Überschrift 2" @click="commands.heading({ level: 3 })" :active="isActive.heading({ level: 3 })">
          <span class="text-body-1 font-weight-bold">Ü2</span>
        </ToolbarButton>
        <ToolbarButton tooltip="Überschrift 3" @click="commands.heading({ level: 4 })" :active="isActive.heading({ level: 4 })">
          <span class="text-body-1 font-weight-bold">Ü3</span>
        </ToolbarButton>
        <ToolbarButton tooltip="Link" @click="showLinkPopup(getMarkAttrs('link').href)" :active="isActive.link()">
          <v-icon>mdi-web</v-icon>
        </ToolbarButton>

        <GenericDialog v-model="dialogLink" :title="dialogLinkUpdating ? 'Link bearbeiten' : 'Link hinzufügen'">
          <template v-slot:content>
            <v-text-field v-model="link" label="URL" placeholder="http://example.com"></v-text-field>
          </template>

          <template v-slot:actions>
            <v-btn @click="cancelLink" color="black" text>
              Abbrechen
            </v-btn>
            <v-btn @click="addLink(commands.link)" color="primary">
              <v-icon left>{{ dialogLinkUpdating ? 'mdi-content-save' : 'mdi-plus' }}</v-icon>
              {{ dialogLinkUpdating ? 'Speichern' : 'Erstellen' }}
            </v-btn>
          </template>
        </GenericDialog>
      </div>
    </editor-menu-bar>
    <div class="pa-4">
      <SimpleTextField class="mb-2" :value="title" @input="$emit('change-title', $event)" :placeholder="titlePlaceholder" :big="true"/>
      <editor-content :editor="editor"/>
    </div>
  </div>
</template>

<script>
import { Editor, EditorContent, EditorMenuBar } from 'tiptap'
import { History, Bold, Italic, Underline, Heading, Link, Placeholder } from 'tiptap-extensions'
import GenericDialog from "@/components/dialog/GenericDialog";
import ToolbarButton from "@/components/ToolbarButton";
import SimpleTextField from "@/components/input/SimpleTextField";

export default {
  name: 'RichEditor',
  components: {SimpleTextField, ToolbarButton, GenericDialog, EditorContent, EditorMenuBar},
  props: {
    title: String,
    titlePlaceholder: String,
    contentPlaceholder: String
  },
  data: () => ({
    editor: null,
    dialogLink: false,
    dialogLinkUpdating: false, // true if updating an existing link
    link: ''
  }),
  methods: {
    showLinkPopup(link) {
      this.link = link;
      this.dialogLinkUpdating = !!link;
      this.dialogLink = true;
    },
    cancelLink() {
      this.link = '';
      this.dialogLink = false;
    },
    addLink(command) {
      command({ href: this.link });
      this.link = '';
      this.dialogLink = false;
    },
    // setContent is ignoring whitespaces, so watching is not possible
    // workaround: use ref
    loadContent(content) {
      this.editor.setContent(content);
    }
  },
  mounted() {
    this.editor = new Editor({
      extensions: [
        new History(),
        new Bold(),
        new Italic(),
        new Underline(),
        new Heading(),
        new Link(),
        new Placeholder({
          emptyNodeText: this.contentPlaceholder,
          emptyNodeClass: 'grey--text text-subtitle-1',
          showOnlyWhenEditable: false,
          showOnlyCurrent: false,
        })
      ],
      onUpdate: () => {
        this.$emit('change-content', this.editor.getHTML());
      },
    })
  },
  beforeDestroy() {
    this.editor.destroy();
    this.editor = null;
  }
}
</script>

<style>
.rich-editor p.is-editor-empty:first-child::before {
  content: attr(data-empty-text);
  float: left;
  pointer-events: none;
  height: 0;
}

.rich-editor ::placeholder {
  color: #9e9e9e;
  opacity: 1; /* Firefox */
}

.rich-editor p {
  margin-bottom: 0;
}

/* hide outline of the editor for chrome browsers */
.ProseMirror {
  outline: none;
}
</style>