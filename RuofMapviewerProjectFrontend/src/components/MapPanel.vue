<template>
  <v-app>
    <HeaderSection
    />
    <v-main>
      
      <MapViewer ref="mapViewer" />
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from "vue";
import MapViewer from "./MapViewer.vue";
import HeaderSection from "./HeaderSection.vue";

const mapViewer = ref(null);


</script>
