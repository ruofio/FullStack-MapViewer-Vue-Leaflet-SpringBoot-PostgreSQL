<template>
  <v-card class="map-container" :class="{ 'fullscreen': isFullScreen }">
    <!-- Welcome Dialog -->
    <v-dialog v-model="showWelcomeDialog" max-width="500">
      <v-card class="welcome-card">
        <v-card-text class="text-center pa-6">
          <v-icon size="64" color="primary" class="mb-4">mdi-map-search</v-icon>
          <h2 class="text-h4 mb-4">Welcome to MapViewer!</h2>
          <p class="text-body-1 mb-6">Are you ready to explore the world?</p>
          <v-btn color="primary" size="large" @click="showWelcomeDialog = false">
            Yes, Let's Begin!
          </v-btn>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-card-text>
      <!-- Search Bar -->
      <v-row>
        <v-col cols="12" sm="6" md="4">
          <v-text-field v-model="searchQuery" label="Search location" prepend-inner-icon="mdi-magnify" clearable
            @keyup="onSearchInput" @click:clear="clearSearchResults" class="search-bar">
            <template v-slot:append>
              <v-btn color="primary" @click="searchLocation" :loading="isSearching" size="large" class="search-button">
                Search
              </v-btn>
            </template>
          </v-text-field>

          <!-- Suggestions List -->
          <v-list v-if="searchResults.length && showSuggestions" dense class="suggestions-list">
            <v-list-item v-for="(result, index) in searchResults" :key="index" @click="selectLocation(result)">
              <v-list-item-content>
                <v-list-item-title>{{ result.display_name }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-col>
      </v-row>


      <v-dialog v-model="showPlaceDialog" max-width="400px">
        <v-card>
          <v-card-title>Add Favorite Place</v-card-title>
          <v-card-text>
            <v-text-field v-model="placeName" label="Place Name"></v-text-field>
            <v-textarea v-model="placeDescription" label="Description"></v-textarea>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="addFavoritePlace">Add Place</v-btn>
            <v-btn @click="cancelAddPlace">Cancel</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <v-dialog v-model="showEditDialog" max-width="400px">
        <v-card>
          <v-card-title>Edit Favorite Place</v-card-title>
          <v-card-text>
            <v-text-field v-model="editingPlace.name" label="Place Name" />
            <v-textarea v-model="editingPlace.description" label="Description" />
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="submitEditPlace">Save</v-btn>
            <v-btn @click="showEditDialog = false">Cancel</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>



      <!-- Favorite Places List -->
      <v-dialog v-model="showFavorites" max-width="600">
        <v-card class="pa-4">
          <v-card-title class="text-h5 font-weight-bold d-flex align-center justify-space-between">
            <span>
              <v-icon color="primary" class="mr-2">mdi-heart-box</v-icon>
              Your Favorite Places
            </span>
            <v-btn variant="text" icon @click="showFavorites = false">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-card-title>

          <v-divider class="mb-3" />

          <v-card-text>
            <v-list density="compact" class="favorite-places-list">
              <v-list-item v-for="place in favoritePlaces" :key="place.id" class="favorite-place-item pa-2 mb-2"
                style="background-color: #f9f9f9; border-radius: 8px;">
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-map-marker-heart</v-icon>
                </template>

                <v-list-item-content>
                  <v-list-item-title class="font-weight-bold text-primary">{{ place.name }}</v-list-item-title>
                  <v-list-item-subtitle>{{ place.description }}</v-list-item-subtitle>
                </v-list-item-content>

                <v-list-item-action class="d-flex justify-end">
                  <v-btn variant="text" icon @click="startEditPlace(place)">
                    <v-icon color="blue">mdi-pencil</v-icon>
                  </v-btn>
                  <v-btn variant="text" icon @click="deletePlace(place.id)">
                    <v-icon color="red">mdi-delete</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>
              <v-list-item v-if="favoritePlaces.length === 0" class="justify-center">
                <v-list-item-content class="text-center text-grey">
                 
                  <v-list-item-title class="mt-2">No favorite places yet ):</v-list-item-title>
                </v-list-item-content>
              </v-list-item>

            </v-list>
          </v-card-text>
        </v-card>
      </v-dialog>


      <!-- Map -->
      <div id="map"></div>

      <!-- Route Panel -->
      <v-expansion-panels class="mt-4">
        <v-expansion-panel>
          <v-expansion-panel-title>
            <v-icon start>mdi-routes</v-icon>
            Route Planning
          </v-expansion-panel-title>
          <v-expansion-panel-text>
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field v-model="startLocation" label="Start Location" prepend-inner-icon="mdi-map-marker-start"
                  clearable />
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field v-model="endLocation" label="End Location" prepend-inner-icon="mdi-map-marker-end"
                  clearable />
              </v-col>
            </v-row>
            <v-btn color="primary" block @click="calculateRoute" :loading="isCalculatingRoute" class="mb-4">
              Calculate Route
            </v-btn>
            <v-btn color="red" block @click="clearRoute" class="mb-4">
              Clear Route
            </v-btn>

            <!-- Route Info -->
            <v-card v-if="routeInfo" variant="outlined" class="mb-4">
              <v-card-text>
                <div class="d-flex align-center mb-2">
                  <v-icon color="primary" class="mr-2">mdi-map-marker-distance</v-icon>
                  <span>Distance: {{ routeInfo.distance }}</span>
                </div>
                <div class="d-flex align-center">
                  <v-icon color="primary" class="mr-2">mdi-clock-outline</v-icon>
                  <span>Duration: {{ routeInfo.duration }}</span>
                </div>
              </v-card-text>
            </v-card>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>

      <!-- Map Controls -->
      <div class="map-controls">
        <v-speed-dial location="left center" transition="fade-transition">
          <template v-slot:activator="{ props: activatorProps }">
            <v-fab v-bind="activatorProps" size="large" color="primary" icon="mdi-plus"></v-fab>
          </template>

          <!-- My Location -->
          <v-tooltip location="top" text="My Location">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="blue-lighten-4" class="map-btn" icon="mdi-crosshairs-gps"
                @click="getCurrentLocation" :loading="isLocating" />
            </template>
          </v-tooltip>

          <!-- Print Map -->
          <v-tooltip location="top" text="Print Map">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="blue-lighten-4" class="map-btn" icon="mdi-printer" @click="printMap" />
            </template>
          </v-tooltip>

          <!-- Reset Map -->
          <v-tooltip location="top" text="Reset Map">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="blue-lighten-4" class="map-btn" icon="mdi-refresh" @click="resetMap" />
            </template>
          </v-tooltip>

          <!-- Favorite Places -->
          <v-tooltip location="top" text="View Favorite Places">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="blue-lighten-4" class="map-btn" icon="mdi-heart"
                @click="showFavorites = true" />
            </template>
          </v-tooltip>
        </v-speed-dial>
      </div>
    </v-card-text>
    <!-- Notifications -->
    <v-snackbar v-model="showError" color="error" timeout="3000">
      {{ errorMessage }}
    </v-snackbar>
    <v-snackbar v-model="showSuccess" color="success" timeout="3000">
      {{ successMessage }}
    </v-snackbar>

    <!-- Footer -->
    <v-footer class="pa-4">
      <v-row justify="center">
        <v-col cols="12" md="6" class="text-center text-black">
          <p>&copy; 2025 MapViewer. All rights reserved.</p>
        </v-col>
      </v-row>
    </v-footer>
  </v-card>
</template>

<script setup>
// Vue Composition API & Leaflet core
import { ref, onMounted, onUnmounted, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Routing and Drawing plugins
import 'leaflet-routing-machine'
import 'leaflet-routing-machine/dist/leaflet-routing-machine.css'
import 'leaflet-draw'
import 'leaflet-draw/dist/leaflet.draw.css'
import 'leaflet-geometryutil'
import axios from 'axios';

axios.defaults.withCredentials = true;
// UI states
const searchQuery = ref('')
const searchResults = ref([])
const startLocation = ref('')
const endLocation = ref('')
const isSearching = ref(false)
const isLocating = ref(false)
const isCalculatingRoute = ref(false)
const showError = ref(false)
const showSuccess = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const routeInfo = ref(null)
const isFullScreen = ref(false)
const showWelcomeDialog = ref(true)
const showSuggestions = ref(true)
const currentLanguage = ref('ar')
const favoritePlaces = ref([]); // Store the list of favorite places
const placeName = ref(''); // Store the new favorite place name
const placeDescription = ref(''); // Store the new favorite place description
const showPlaceDialog = ref(false); // Control the dialog visibility
const userId = ref(1); // Assume user is logged in with ID 1 (this would come from your authentication system)
const showFavorites = ref(false);
const editingPlace = ref(null);
const showEditDialog = ref(false);
const isDrawingActive = ref(false);

// Leaflet map elements
let map = null
let currentMarker = null
let locationMarker = null
let routingControl = null
let streetLayer = null
let satelliteLayer = null
let terrainLayer = null
let layerControl = null
let drawnItems = null
let currentBaseLayer = null

// ============================
// Fullscreen Control
// ============================
const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value;
  const button = document.querySelector('.leaflet-control-fullscreen-button i');

  const mapContainer = document.querySelector('.map-container');
  if (!document.fullscreenElement) {
    if (mapContainer.requestFullscreen) mapContainer.requestFullscreen();
    else if (mapContainer.mozRequestFullScreen) mapContainer.mozRequestFullScreen();
    else if (mapContainer.webkitRequestFullscreen) mapContainer.webkitRequestFullscreen();
    else if (mapContainer.msRequestFullscreen) mapContainer.msRequestFullscreen();
    if (button) button.className = 'fas fa-compress';
  } else {
    if (document.exitFullscreen) document.exitFullscreen();
    else if (document.mozCancelFullScreen) document.mozCancelFullScreen();
    else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
    else if (document.msExitFullscreen) document.msExitFullscreen();
    if (button) button.className = 'fas fa-expand';
  }

  setTimeout(() => {
    if (map) map.invalidateSize();
  }, 100);
};

const handleFullscreenChange = () => {
  isFullScreen.value = !!document.fullscreenElement;
};

// ============================
// Search & Location Handlers
// ============================
const searchLocation = async () => {
  if (!searchQuery.value) return;
  isSearching.value = true;
  try {
    const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}`);
    const data = await response.json();
    if (data.length === 0) {
      errorMessage.value = 'Location not found';
      showError.value = true;
      return;
    }
    const location = data[0];
    const coords = L.latLng(location.lat, location.lon);
    map.setView(coords, 13);
    addMarker(coords, location.display_name);
  } catch (error) {
    errorMessage.value = 'Error searching location';
    showError.value = true;
  } finally {
    isSearching.value = false;
  }
};

const onSearchInput = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }
  isSearching.value = true;
  try {
    const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=5`);
    const data = await response.json();
    searchResults.value = data;
  } catch (error) {
    console.error('Error fetching search results:', error);
  } finally {
    isSearching.value = false;
  }
};

const selectLocation = (location) => {
  searchQuery.value = location.display_name;
  searchResults.value = [];
  const coords = L.latLng(location.lat, location.lon);
  map.setView(coords, 13);
  addMarker(coords, `Location: ${location.display_name}`);
};

const clearSearchResults = () => {
  searchQuery.value = '';
  searchResults.value = [];
};

const addMarker = (coords, description) => {
  if (currentMarker) map.removeLayer(currentMarker);
  currentMarker = L.marker(coords).addTo(map).bindPopup(description).openPopup();
};

const getCurrentLocation = () => {
  isLocating.value = true;
  if (!navigator.geolocation) {
    errorMessage.value = 'Geolocation is not supported by your browser';
    showError.value = true;
    isLocating.value = false;
    return;
  }
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords;
      addMarker([latitude, longitude], "You are here!");
      if (locationMarker) map.removeLayer(locationMarker);
      locationMarker = L.marker([latitude, longitude], {
        icon: L.divIcon({
          className: 'current-location-marker',
          html: '<div class="ping"></div>'
        })
      }).addTo(map);
      map.setView([latitude, longitude], 13);
      isLocating.value = false;
    },
    () => {
      errorMessage.value = 'Failed to retrieve location';
      showError.value = true;
      isLocating.value = false;
    }
  );
};

// ============================
// Routing Handlers
// ============================
const calculateRoute = async () => {
  if (!startLocation.value || !endLocation.value) {
    errorMessage.value = 'Please enter both start and end locations';
    showError.value = true;
    return;
  }

  isCalculatingRoute.value = true;
  try {
    const startResponse = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(startLocation.value)}`);
    const startData = await startResponse.json();
    const endResponse = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(endLocation.value)}`);
    const endData = await endResponse.json();

    if (startData.length === 0 || endData.length === 0) {
      throw new Error('One or both locations not found');
    }

    const startCoords = L.latLng(startData[0].lat, startData[0].lon);
    const endCoords = L.latLng(endData[0].lat, endData[0].lon);

    if (routingControl) map.removeControl(routingControl);

    routingControl = L.Routing.control({
      waypoints: [startCoords, endCoords],
      routeWhileDragging: true,
      showAlternatives: true,
      lineOptions: {
        styles: [{ color: '#1976D2', weight: 4 }]
      }
    }).addTo(map);

    routingControl.on('routesfound', (e) => {
      const route = e.routes[0];
      routeInfo.value = {
        distance: (route.summary.totalDistance / 1000).toFixed(2) + ' km',
        duration: Math.round(route.summary.totalTime / 60) + ' minutes'
      };
    });

    map.fitBounds(L.latLngBounds([startCoords, endCoords]), { padding: [50, 50] });
  } catch (error) {
    errorMessage.value = error.message || 'Error calculating route';
    showError.value = true;
  } finally {
    isCalculatingRoute.value = false;
  }
};

const clearRoute = () => {
  if (routingControl) map.removeControl(routingControl);
  routeInfo.value = null;
};

const printMap = () => window.print();

// ============================
// Map Reset & Language Helpers
// ============================
const resetMap = () => {
  const lang = languageConfig[currentLanguage.value];
  if (lang && lang.regions) {
    if (lang.regions.length === 2) map.fitBounds(lang.regions);
    else map.setView(lang.regions[0], 5);
  } else {
    map.setView([23.8859, 45.0792], 5); // fallback
  }

  if (currentMarker) map.removeLayer(currentMarker);
  if (locationMarker) map.removeLayer(locationMarker);
  if (routingControl) map.removeControl(routingControl);
  routeInfo.value = null;
};



onMounted(() => {
  // Get language from URL or localStorage
  const savedLang = localStorage.getItem('mapLanguage') || 'ar'
  currentLanguage.value = savedLang

  map = L.map('map', {
    center: [24.7136, 46.6753],
    zoom: 5,
    minZoom: 0,
    maxZoom: 19,
    zoomControl: false,
    maxBoundsViscosity: 1.0,
    worldCopyJump: true
  })

  map.createPane('base')
  map.getPane('base').style.zIndex = 200

  // Initialize map events
  map.on('click', onMapClick);

  // Fetch the user's favorite places after the map is mounted
  fetchFavoritePlaces();

  //full screen
  L.Control.Fullscreen = L.Control.extend({
    options: {
      position: 'topleft'
    },

    onAdd: function (map) {
      const container = L.DomUtil.create('div', 'leaflet-control-fullscreen leaflet-bar leaflet-control');
      const button = L.DomUtil.create('a', 'leaflet-control-fullscreen-button', container);
      button.href = '#';
      button.title = 'Full Screen';
      button.innerHTML = '<i class="fas fa-expand"></i>';

      L.DomEvent.on(button, 'click', L.DomEvent.stopPropagation)
        .on(button, 'click', L.DomEvent.preventDefault)
        .on(button, 'click', () => {
          toggleFullScreen();
        });

      return container;
    }
  });

  L.control.fullscreen = function (opts) {
    return new L.Control.Fullscreen(opts);
  }

  // Add the fullscreen control to the map
  L.control.fullscreen({ position: 'topleft' }).addTo(map);
  L.Control.Language = L.Control.extend({
    options: {
      position: 'topleft'
    },

    onAdd: function (map) {
      const container = L.DomUtil.create('div', 'leaflet-control-language leaflet-bar leaflet-control');
      const button = L.DomUtil.create('a', 'leaflet-control-language-button', container);
      const dropdown = L.DomUtil.create('div', 'language-dropdown', container);

      button.href = '#';
      button.innerHTML = `<span>${currentLanguage.value.toUpperCase()}</span>`;

      Object.values(languageConfig).forEach(lang => {
        const item = L.DomUtil.create('a', 'language-item', dropdown);
        item.href = '#';
        item.innerHTML = lang.name;

        L.DomEvent.on(item, 'click', function (e) {
          L.DomEvent.stop(e);
          currentLanguage.value = lang.code;
          button.innerHTML = `<span>${lang.code.toUpperCase()}</span>`;
          dropdown.style.display = 'none';
        });
      });

      L.DomEvent.on(button, 'click', function (e) {
        L.DomEvent.stop(e);
        dropdown.style.display = dropdown.style.display === 'none' ? 'block' : 'none';
      });

      L.DomEvent.on(document, 'click', function () {
        dropdown.style.display = 'none';
      });

      return container;
    }
  });

  L.control.language = function (opts) {
    return new L.Control.Language(opts);
  }
  L.control.language({ position: 'topleft' }).addTo(map);
  changeMapLanguage(savedLang);

  // Create custom panes for better layer management
  map.createPane('base')
  map.getPane('base').style.zIndex = 200

  streetLayer = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19,
    minZoom: 0,
    pane: 'base',
    bounds: [[-90, -180], [90, 180]],
    tileSize: 256,
    noWrap: false,
    tms: false,
    worldCopyJump: true,
    continuousWorld: true,
    zoomOffset: 0
  })

  satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles © Esri',
    maxZoom: 19,
    minZoom: 0,
    pane: 'base',
    bounds: [[-90, -180], [90, 180]],
    tileSize: 256,
    noWrap: false,
    tms: false,
    worldCopyJump: true,
    continuousWorld: true,
    zoomOffset: 0
  })

  terrainLayer = L.tileLayer("https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png", {
    attribution: '© OpenTopoMap contributors',
    maxZoom: 17,
    minZoom: 0,
    pane: 'base',
    bounds: [[-90, -180], [90, 180]],
    tileSize: 256,
    noWrap: false,
    tms: false,
    worldCopyJump: true,
    continuousWorld: true,
    zoomOffset: 0
  })

  // Add this extension to handle world copies
  L.GridLayer.prototype._setView = function (center, zoom, noPrune, noUpdate) {
    var tileZoom = Math.round(zoom);
    if ((this.options.maxZoom !== undefined && tileZoom > this.options.maxZoom) ||
      (this.options.minZoom !== undefined && tileZoom < this.options.minZoom)) {
      tileZoom = undefined;
    }

    var tileZoomChanged = this.options.updateWhenZooming && (tileZoom !== this._tileZoom);

    if (!noUpdate || tileZoomChanged) {
      this._tileZoom = tileZoom;

      if (this._abortLoading) {
        this._abortLoading();
      }

      this._updateLevels();
      this._resetGrid();

      if (tileZoom !== undefined) {
        this._update(center);
      }

      if (!noPrune) {
        this._pruneTiles();
      }

      this._noPrune = !!noPrune;
    }

    this._setZoomTransforms(center, zoom);
  }

  // Try alternative tile servers for better coverage
  const alternativeStreetLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    attribution: '© OpenStreetMap contributors, © CARTO',
    maxZoom: 19,
    minZoom: 0,
    pane: 'base',
    bounds: [[-90, -180], [90, 180]],
    tileSize: 256,
    noWrap: false,
    tms: false,
    worldCopyJump: true,
    continuousWorld: true,
    zoomOffset: 0
  })

  // Add the layers to the map
  alternativeStreetLayer.addTo(map)

  const baseLayers = {
    "Streets (Default)": alternativeStreetLayer,
    "Streets (OSM)": streetLayer,
    "Satellite": satelliteLayer,
    "Terrain": terrainLayer
  }

  const overlayLayers = {
    "Traffic": L.tileLayer('https://tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }),
    "Buildings": L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    })
  }

  layerControl = L.control.layers(baseLayers, overlayLayers, { position: 'topright' }).addTo(map)
  L.control.scale({ position: 'bottomright' }).addTo(map)

  // Initialize FeatureGroup for drawn items
  drawnItems = new L.FeatureGroup()
  map.addLayer(drawnItems)

  // Initialize FeatureGroup for drawn items
  drawnItems = new L.FeatureGroup();
  map.addLayer(drawnItems);

  // Initialize draw control without rectangle
  const drawControl = new L.Control.Draw({
    position: 'topleft',
    draw: {
      polygon: {
        allowIntersection: false,
        showArea: true
      },
      polyline: {
        metric: true
      },
      circle: {
        metric: true
      },
      rectangle: false, // Disable rectangle here
      circlemarker: false,
      marker: true
    },
    edit: {
      featureGroup: drawnItems,
      remove: true
    }
  });

  map.addControl(drawControl);
  // Track drawing and editing mode to disable favorite place dialog
  map.on('draw:drawstart', () => {
    isDrawingActive.value = true;
    showPlaceDialog.value = false;
  });

  map.on('draw:drawstop', () => {
    isDrawingActive.value = false;
  });

  map.on('draw:editstart', () => {
    isDrawingActive.value = true;
    showPlaceDialog.value = false;
  });

  map.on('draw:editstop', () => {
    isDrawingActive.value = false;
  });

  // Handle draw events
  map.on('draw:created', async (e) => {
    const layer = e.layer;
    const type = e.layerType;

    if (type === 'marker') {
      const coords = layer.getLatLng();
      try {
        // Fetch location information using reverse geocoding
        const response = await fetch(
          `https://nominatim.openstreetmap.org/reverse?format=json&lat=${coords.lat}&lon=${coords.lng}`
        );
        const data = await response.json();

        // Create detailed popup content
        const popupContent = `
        <div class="marker-popup">
          <h5 class="text-lg font-bold mb-2">${data.display_name}</h5>
          <div class="marker-details">
            ${data.address ? `
              <p><strong>Address:</strong> ${data.address.road || ''} ${data.address.house_number || ''}</p>
              <p><strong>City:</strong> ${data.address.city || data.address.town || data.address.village || ''}</p>
              <p><strong>State:</strong> ${data.address.state || ''}</p>
              <p><strong>Country:</strong> ${data.address.country || ''}</p>
              <p><strong>Postal Code:</strong> ${data.address.postcode || ''}</p>
            ` : ''}
            <p><strong>Coordinates:</strong> ${coords.lat.toFixed(6)}, ${coords.lng.toFixed(6)}</p>
          </div>
        </div>
      `;

        layer.bindPopup(popupContent, {
          maxWidth: 300,
          className: 'custom-popup'
        }).openPopup();
      } catch (error) {
        console.error('Error fetching location details:', error);
        // Fallback popup if reverse geocoding fails
        layer.bindPopup(`
        <div class="marker-popup">
          <h3>Location Details</h3>
          <p><strong>Coordinates:</strong> ${coords.lat.toFixed(6)}, ${coords.lng.toFixed(6)}</p>
        </div>
      `).openPopup();
      }
    } else if (type === 'polygon' || type === 'rectangle') {
      const area = L.GeometryUtil.geodesicArea(layer.getLatLngs()[0]);
      layer.bindPopup(`Area: ${(area / 1000000).toFixed(2)} km²`);
    } else if (type === 'polyline') {
      const latlngs = layer.getLatLngs();
      let length = 0;
      for (let i = 1; i < latlngs.length; i++) {
        length += latlngs[i - 1].distanceTo(latlngs[i]);
      }
      layer.bindPopup(`Length: ${(length / 1000).toFixed(2)} km`);
    } else if (type === 'circle') {
      const radius = layer.getRadius();
      const area = Math.PI * radius * radius;
      layer.bindPopup(`Radius: ${(radius / 1000).toFixed(2)} km<br>Area: ${(area / 1000000).toFixed(2)} km²`);
    }

    drawnItems.addLayer(layer);
  });

  // Handle editing of existing markers
  map.on('draw:edited', async (e) => {
    const layers = e.layers;
    layers.eachLayer(async (layer) => {
      if (layer instanceof L.Marker) {
        const coords = layer.getLatLng();
        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${coords.lat}&lon=${coords.lng}`
          );
          const data = await response.json();

          const popupContent = `
          <div class="marker-popup">
            <h5 class="text-lg font-bold mb-2">${data.display_name}</5>
            <div class="marker-details">
              ${data.address ? `
                <p><strong>Address:</strong> ${data.address.road || ''} ${data.address.house_number || ''}</p>
                <p><strong>City:</strong> ${data.address.city || data.address.town || data.address.village || ''}</p>
                <p><strong>State:</strong> ${data.address.state || ''}</p>
                <p><strong>Country:</strong> ${data.address.country || ''}</p>
                <p><strong>Postal Code:</strong> ${data.address.postcode || ''}</p>
              ` : ''}
              <p><strong>Coordinates:</strong> ${coords.lat.toFixed(6)}, ${coords.lng.toFixed(6)}</p>
            </div>
          </div>
        `;

          layer.setPopupContent(popupContent);
          layer.openPopup();
        } catch (error) {
          console.error('Error updating location details:', error);
        }
      } else if (layer instanceof L.Polygon || layer instanceof L.Rectangle) {
        const area = L.GeometryUtil.geodesicArea(layer.getLatLngs()[0]);
        layer.setPopupContent(`Area: ${(area / 1000000).toFixed(2)} km²`);
      } else if (layer instanceof L.Polyline) {
        const latlngs = layer.getLatLngs();
        let length = 0;
        for (let i = 1; i < latlngs.length; i++) {
          length += latlngs[i - 1].distanceTo(latlngs[i]);
        }
        layer.setPopupContent(`Length: ${(length / 1000).toFixed(2)} km`);
      }
    });
  });

})
//Language configurations
const languageConfig = {
  ar: {
    code: 'ar',
    name: 'AR',
    dir: 'rtl',
    mapUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    regions: [[12, 35], [32, 55]] // Middle East
  },
  en: {
    code: 'en',
    name: 'EN',
    dir: 'ltr',
    mapUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    regions: [[51.5074, -0.1278]] // London
  },
  fr: {
    code: 'fr',
    name: 'FR',
    dir: 'ltr',
    mapUrl: 'https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png',
    regions: [[41, -5], [51, 10]]
  },
  es: {
    code: 'es',
    name: 'ES',
    dir: 'ltr',
    mapUrl: 'https://{s}.tile.openstreetmap.de/{z}/{x}/{y}.png',
    regions: [[36, -10], [44, 5]]
  },
  zh: {
    code: 'zh',
    name: '中文',
    dir: 'ltr',
    mapUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    regions: [[18, 73], [54, 135]]
  }
}



watch(currentLanguage, (newLang) => {
  localStorage.setItem('mapLanguage', newLang);
  changeMapLanguage(newLang);
});

//Function to handle language changes
const changeMapLanguage = (langCode) => {
  if (!languageConfig[langCode]) {
    console.warn(`Language not found: ${langCode}`)
    return
  }

  const lang = languageConfig[langCode]
  currentLanguage.value = langCode
  document.documentElement.dir = lang.dir
  localStorage.setItem('mapLanguage', langCode)

  // Remove current base layer if exists
  if (currentBaseLayer && map.hasLayer(currentBaseLayer)) {
    map.removeLayer(currentBaseLayer)
  }

  // Add new base layer
  currentBaseLayer = L.tileLayer(lang.mapUrl, {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19,
    minZoom: 0,
    pane: 'base'
  })
  currentBaseLayer.addTo(map)

  // Set region
  if (lang.regions && lang.regions.length === 2) {
    map.fitBounds(lang.regions)
  } else {
    map.setView(lang.regions[0], 5)
  }
}
// ============================
// Map Interaction
// ============================
const onMapClick = (event) => {
  if (isDrawingActive.value) return; // Prevent showing dialog while drawing

  showPlaceDialog.value = true;
  currentMarker = L.marker(event.latlng).addTo(map).bindPopup('Click to add favorite place').openPopup();
};


// Favorite Places Logic

// ============================
// Fetch Favorite Places
// ============================
const favoritePlaceMarkers = [];

const fetchFavoritePlaces = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/favorite-places/my`, {
      method: 'GET',
      credentials: 'include'
    });
    if (!response.ok) throw new Error('Failed to fetch favorites');
    const data = await response.json();
    favoritePlaces.value = data;

    // Clear old markers
    favoritePlaceMarkers.forEach(marker => map.removeLayer(marker));
    favoritePlaceMarkers.length = 0;

    // Add markers with custom heart icon
    data.forEach(place => {
      const heartIcon = L.divIcon({
        html: '<i class="mdi mdi-heart" style="color:#1976D2;font-size:24px;"></i>',
        iconSize: [24, 24],
        className: 'favorite-heart-icon'
      });

      const marker = L.marker([place.latitude, place.longitude], { icon: heartIcon })
        .bindPopup(`<b>${place.name}</b><br>${place.description}`)
        .addTo(map);

      favoritePlaceMarkers.push(marker);
    });

  } catch (error) {
    console.error('Error fetching favorite places:', error);
    showError.value = true;
    errorMessage.value = 'Could not load your favorite places.';
  }
};

const startEditPlace = (place) => {
  editingPlace.value = { ...place }; // clone to avoid binding issues
  showEditDialog.value = true;
};

const submitEditPlace = async () => {
  try {
    await axios.put(`http://localhost:8080/api/favorite-places/update/${editingPlace.value.id}`, editingPlace.value, {
      withCredentials: true
    });
    showSuccess.value = true;
    successMessage.value = 'Place updated successfully!';
    showEditDialog.value = false;
    fetchFavoritePlaces();
  } catch (error) {
    console.error('Failed to update place:', error);
    showError.value = true;
    errorMessage.value = 'Could not update the place.';
  }
};

// ============================
// Add, Delete, Edit Favorite Places
// ============================
const addFavoritePlace = async () => {
  if (!placeName.value || !placeDescription.value || !currentMarker) {
    showError.value = true;
    errorMessage.value = 'Please provide place name and description.';
    return;
  }

  const newPlace = {
    name: placeName.value,
    description: placeDescription.value,
    latitude: currentMarker.getLatLng().lat,
    longitude: currentMarker.getLatLng().lng
  };

  try {
    const response = await fetch('http://localhost:8080/api/favorite-places/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(newPlace)
    });

    if (!response.ok) throw new Error('Failed to add');

    const savedPlace = await response.json(); // Get the saved place with ID

    // Add marker manually with heart icon
    const heartIcon = L.divIcon({
      html: '<i class="mdi mdi-heart" style="color:#1976D2;font-size:24px;"></i>',
      iconSize: [24, 24],
      className: 'favorite-heart-icon'
    });

    const marker = L.marker([savedPlace.latitude, savedPlace.longitude], { icon: heartIcon })
      .bindPopup(`<b>${savedPlace.name}</b><br>${savedPlace.description}`)
      .addTo(map);

    favoritePlaceMarkers.push(marker);
    favoritePlaces.value.push(savedPlace); // Optional: update list reactively

    showSuccess.value = true;
    successMessage.value = 'Place added successfully!';
  } catch (error) {
    console.error('Error adding favorite place:', error);
    showError.value = true;
    errorMessage.value = 'Error adding the place.';
  } finally {
    placeName.value = '';
    placeDescription.value = '';
    showPlaceDialog.value = false;

    if (currentMarker) {
      map.removeLayer(currentMarker);
      currentMarker = null;
    }
  }
};

const deletePlace = async (placeId) => {
  try {
    await axios.delete(`http://localhost:8080/api/favorite-places/delete/${placeId}`, {
      withCredentials: true
    });
    fetchFavoritePlaces();
  } catch (error) {
    console.error('Failed to delete place:', error);
    showError.value = true;
    errorMessage.value = 'Could not delete the place.';
  }
};
const cancelAddPlace = () => {
  showPlaceDialog.value = false;

  // Remove the marker if it exists
  if (currentMarker) {
    map.removeLayer(currentMarker);
    currentMarker = null;
  }
};


onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.removeEventListener('mozfullscreenchange', handleFullscreenChange)
  document.removeEventListener('MSFullscreenChange', handleFullscreenChange)
})
</script>

<style scoped>
.map-container {
  position: relative;
  transition: all 0.3s ease;
}

.map-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1000;
  /* Make sure it's above the map */
}

.welcome-card {
  background: rgba(255, 255, 255, 0.474) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  background: white;
}

.fullscreen #map {
  height: calc(100vh - 100px);
  width: 100%;
}

.fullscreen-toggle {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1001;
  background: rgba(255, 255, 255, 0.8);
}

.fullscreen .v-card-text {
  height: 100%;
}

#map {
  height: 700px;
  width: 100%;
  transition: height 0.3s ease;
  background: #f2f2f2;
}

.leaflet-layer {
  will-change: transform;
  image-rendering: -webkit-optimize-contrast;
}

.leaflet-tile-container {
  will-change: transform;
  transform-origin: 0 0;
}

.leaflet-tile {
  box-shadow: none !important;
}

.map-controls {
  display: flex;
  justify-content: space-evenly;
  margin-top: 16px;
}

.map-btn {
  flex-grow: 1;
  font-size: 12px;
  padding: 6px;
}

.search-bar {
  margin-bottom: 4px;
}

.search-button {
  font-size: 16px;
  padding: 10px 22px;
  min-width: 120px;
}

.suggestions-list {
  margin-top: -28px;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.v-expansion-panels.route-panel {
  max-width: 600px;
  margin: 0 auto;
}

.v-text-field {
  margin-bottom: 16px;
}

.v-btn {
  margin: 0 5px;
}

/* Draw control styles */
.leaflet-draw-tooltip {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid #000;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.leaflet-draw-actions {
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.leaflet-draw-actions a {
  color: #333;
  text-decoration: none;
  padding: 8px 12px;
  display: block;
  border-bottom: 1px solid #eee;
}

.leaflet-draw-actions a:last-child {
  border-bottom: none;
}

.leaflet-draw-actions a:hover {
  background-color: #f5f5f5;
}

/* Current location marker animation */
.current-location-marker {
  position: relative;
}

.ping {
  width: 16px;
  height: 16px;
  background: #1976D2;
  border-radius: 50%;
  position: relative;
}

.ping::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #1976D2;
  border-radius: 50%;
  animation: ping 1.5s ease-out infinite;
}

@keyframes ping {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  100% {
    transform: scale(3);
    opacity: 0;
  }
}

@media print {

  .v-card-title,
  .v-text-field,
  .map-controls,
  .v-snackbar,
  .v-expansion-panels,
  .leaflet-control-layers,
  .leaflet-draw {
    display: none !important;
  }

  #map {
    height: 100vh !important;
    margin: 0 !important;
  }

  .map-container {
    margin: 0 !important;
  }
}

/* Layer control styles */
.leaflet-control-layers {
  background: white;
  padding: 6px;
  border-radius: 4px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.leaflet-control-layers-toggle {
  width: 36px;
  height: 36px;
  background-size: 20px 20px;
}

.leaflet-control-layers-expanded {
  padding: 6px 10px 6px 6px;
  min-width: 200px;
}

.leaflet-control-layers-list {
  margin: 0;
  padding: 0;
}

/* Scale control styles */
.leaflet-control-scale {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 2px 5px;
  border-radius: 2px;
}

/* Draw measurement result popup styles */
.leaflet-popup-content {
  margin: 8px 12px;
  font-size: 14px;
  line-height: 1.4;
}

.leaflet-popup-content-wrapper {
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.leaflet-popup-tip-container {
  margin-top: -1px;
}

.leaflet-control-fullscreen {
  background: white;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.leaflet-control-fullscreen-button {
  width: 30px;
  height: 30px;
  background-position: 50% 50%;
  background-repeat: no-repeat;
  display: block;
  text-align: center;
  line-height: 30px;
  color: #333;
  text-decoration: none;
}

.leaflet-control-fullscreen-button:hover {
  background-color: #f4f4f4;
}

/* Add Font Awesome icon styles */
.leaflet-control-fullscreen-button i {
  font-size: 16px;
}

.leaflet-control-language {
  background: white;
  padding: 0;
  border-radius: 4px;
  position: relative;
}

.leaflet-control-language-button {
  width: 30px;
  height: 30px;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  line-height: 30px;
  display: block;
  text-decoration: none;
  color: #333;
}

.leaflet-control-language-button:hover {
  background-color: #f4f4f4;
}

.language-dropdown {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  margin-top: 5px;
  min-width: 120px;
}

.language-item {
  display: block;
  padding: 8px 12px;
  color: #333;
  text-decoration: none;
  border-bottom: 1px solid #eee;
}

.language-item:last-child {
  border-bottom: none;
}

.language-item:hover {
  background-color: #f4f4f4;
}

[dir="rtl"] .leaflet-control-language {
  margin-right: 10px;
}

[dir="rtl"] .language-dropdown {
  right: 0;
  left: auto;
}

[dir="rtl"] .leaflet-control-scale {
  margin-right: 5px;
}

[dir="rtl"] .v-expansion-panel-title .v-icon {
  margin-left: 8px;
  margin-right: 0;
}

/* Fullscreen adjustments */
.fullscreen .leaflet-control-language {
  margin-top: 10px;
}

.fullscreen .v-expansion-panels,
.fullscreen .map-controls,
.fullscreen .v-snackbar,
.fullscreen .v-footer {
  display: none !important;
}

/* Favorite Places List */
.favorite-places-list {
  margin-top: 8px;
  padding: 0 4px;
  max-height: 350px;
  overflow-y: auto;
}

.favorite-place-item {
  transition: box-shadow 0.2s;
}

.favorite-place-item:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  background-color: #f3f6fc !important;
}

.v-list-item-title {
  font-size: 16px;
}

.v-list-item-subtitle {
  font-size: 14px;
  color: #666;
}

</style>
