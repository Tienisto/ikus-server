<template>
  <div style="min-height: 400px;">
    <v-sheet color="secondary">
      <ToolbarButton tooltip="Rückgängig" icon="mdi-undo"
                     @click="editor!.commands.undo()" :active="false" />
      <ToolbarButton tooltip="Wiederherstellen" icon="mdi-redo"
                     @click="editor!.commands.redo()" :active="false" />
      <ToolbarButton tooltip="Fett" icon="mdi-format-bold"
                     @click="editor!.chain().focus().toggleBold().run()" :active="editor?.isActive('bold')" />
      <ToolbarButton tooltip="Kursiv" icon="mdi-format-italic"
                     @click="editor!.chain().focus().toggleItalic().run()" :active="editor?.isActive('italic')" />
      <ToolbarButton tooltip="Unterstrichen" icon="mdi-format-underline"
                     @click="editor!.chain().focus().toggleUnderline().run()" :active="editor?.isActive('underline')" />
      <ToolbarButton tooltip="Überschrift 1" icon="mdi-format-header-1"
                     @click="editor!.chain().focus().toggleHeading({ level: 1 }).run()" :active="editor?.isActive('heading', { level: 1 })" />
      <ToolbarButton tooltip="Überschrift 2" icon="mdi-format-header-2"
                     @click="editor!.chain().focus().toggleHeading({ level: 2 }).run()" :active="editor?.isActive('heading', { level: 2 })" />
      <ToolbarButton tooltip="Überschrift 3" icon="mdi-format-header-3"
                     @click="editor!.chain().focus().toggleHeading({ level: 3 }).run()" :active="editor?.isActive('heading', { level: 3 })" />
      <ToolbarButton tooltip="Link" icon="mdi-link"
                     @click="toggleLink" :active="editor?.isActive('link')" />
    </v-sheet>
    <div class="pa-4">
      <SimpleTextField class="mb-2" v-model="title" :placeholder="props.titlePlaceholder" :big="true"/>
      <editor-content :editor="editor"/>
    </div>

    <GenericDialog v-model="dialogLink" :title="dialogLinkUpdating ? 'Link bearbeiten' : 'Link hinzufügen'">
      <template v-slot:content>
        <v-text-field v-model="link" label="URL" placeholder="https://example.com"></v-text-field>
      </template>

      <template v-slot:actions>
        <v-btn @click="cancelLink" color="black">
          Abbrechen
        </v-btn>
        <v-btn @click="addLink" color="primary" :prepend-icon="dialogLinkUpdating ? 'mdi-content-save' : 'mdi-plus'" variant="elevated">
          {{ dialogLinkUpdating ? 'Speichern' : 'Erstellen' }}
        </v-btn>
      </template>
    </GenericDialog>
  </div>
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import SimpleTextField from "~/components/input/SimpleTextField.vue";
import GenericDialog from "~/components/dialog/GenericDialog.vue";
import ToolbarButton from "~/components/input/editor/ToolbarButton.vue";
import {Placeholder} from "@tiptap/extension-placeholder";
import {Underline} from "@tiptap/extension-underline";
import {Link} from "@tiptap/extension-link";
import {ref} from "vue";

const emit = defineEmits(['update:content']);
const title = defineModel<string>('title', { required: true });
const props = defineProps<{
  titlePlaceholder: string,
  contentPlaceholder: string,
  content: string
}>();

const editor = useEditor({
  content: '',
  extensions: [
    StarterKit,
    Placeholder.configure({
      emptyEditorClass: 'is-editor-empty',
      placeholder: props.contentPlaceholder,
    }),
    Underline,
    Link,
  ],
  editorProps: {
    attributes: {
      class: 'focus:outline-none',
    },
  },
  onUpdate: () => {
    emit('update:content', editor.value?.getHTML())
  },
  watch: {
    content: function (newContent: string) {
      const isSame = editor.value?.getHTML() === newContent
      if (isSame) {
        return
      }

      editor.value?.commands.setContent(newContent, false)
    }
  }
});

const loadContent = (content: string) => {
  editor.value?.commands.setContent(content, false)
}

// Links
const dialogLink = ref(false);
const dialogLinkUpdating = ref(false);
const link = ref('');

const toggleLink = () => {
  if (editor.value?.isActive('link')) {
    editor.value?.chain().focus().unsetLink().run();
  } else {
    const existingLink = editor.value?.getAttributes('link').href;
    link.value = existingLink;
    dialogLinkUpdating.value = !!existingLink;
    dialogLink.value = true;
  }
}

const cancelLink = () => {
  link.value = '';
  dialogLink.value = false;
}

const addLink = () => {
  editor.value?.chain().focus().setLink({ href: link.value }).run();
  link.value = '';
  dialogLink.value = false;
}

defineExpose({ loadContent });
</script>

<style>
/* https://tiptap.dev/docs/editor/extensions/functionality/placeholder */
.tiptap p.is-editor-empty:first-child::before {
  color: #adb5bd;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

.outline-none:focus {
  outline: none;
}

/* Remove focus border https://github.com/ueberdosis/tiptap/issues/526 */
.ProseMirror:focus {
  outline: none;
}
</style>
