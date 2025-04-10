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
                  clearable></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field v-model="endLocation" label="End Location" prepend-inner-icon="mdi-map-marker-end"
                  clearable></v-text-field>
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
import { ref, onMounted, onUnmounted } from 'vue'
import L from "leaflet"
import "leaflet/dist/leaflet.css"
import "leaflet-routing-machine"
import "leaflet-routing-machine/dist/leaflet-routing-machine.css"
import 'leaflet-draw';
import 'leaflet-draw/dist/leaflet.draw.css';
import 'leaflet-geometryutil';

// State variables
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
const currentLanguage = ref('en')
const languages = [
  { code: 'en', name: 'English' },
  { code: 'ar', name: 'العربية' },
  { code: 'fr', name: 'Français' },
  { code: 'es', name: 'Español' },
  { code: 'zh', name: '中文' }
]

// Map and control variables
let map = null
let currentMarker = null
let locationMarker = null
let routingControl = null
let streetLayer = null
let satelliteLayer = null
let terrainLayer = null
let layerControl = null
let drawnItems = null

// Full-screen toggle method
const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value;
  const button = document.querySelector('.leaflet-control-fullscreen-button i');
  
  if (!document.fullscreenElement) {
    const mapContainer = document.querySelector('.map-container');
    if (mapContainer.requestFullscreen) {
      mapContainer.requestFullscreen();
    } else if (mapContainer.mozRequestFullScreen) {
      mapContainer.mozRequestFullScreen();
    } else if (mapContainer.webkitRequestFullscreen) {
      mapContainer.webkitRequestFullscreen();
    } else if (mapContainer.msRequestFullscreen) {
      mapContainer.msRequestFullscreen();
    }
    if (button) button.className = 'fas fa-compress';
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    }
    if (button) button.className = 'fas fa-expand';
  }

  setTimeout(() => {
    if (map) {
      map.invalidateSize();
    }
  }, 100);
};

const handleFullscreenChange = () => {
  isFullScreen.value = !!document.fullscreenElement
}

// Search and location functions
const searchLocation = async () => {
  if (!searchQuery.value) return
  isSearching.value = true
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}`
    )
    const data = await response.json()
    if (data.length === 0) {
      errorMessage.value = 'Location not found'
      showError.value = true
      return
    }
    const location = data[0]
    const coords = L.latLng(location.lat, location.lon)
    map.setView(coords, 13)
    addMarker(coords, location.display_name)
  } catch (error) {
    errorMessage.value = 'Error searching location'
    showError.value = true
  } finally {
    isSearching.value = false
  }
}

const onSearchInput = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  isSearching.value = true
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=5`
    )
    const data = await response.json()
    searchResults.value = data
  } catch (error) {
    console.error('Error fetching search results:', error)
  } finally {
    isSearching.value = false
  }
}

const selectLocation = (location) => {
  searchQuery.value = location.display_name
  searchResults.value = []
  const coords = L.latLng(location.lat, location.lon)
  map.setView(coords, 13)
  addMarker(coords, `Location: ${location.display_name}`)
}

const clearSearchResults = () => {
  searchQuery.value = ''
  searchResults.value = []
}

const addMarker = (coords, description) => {
  if (currentMarker) {
    map.removeLayer(currentMarker)
  }
  currentMarker = L.marker(coords)
    .addTo(map)
    .bindPopup(description)
    .openPopup()
}

const getCurrentLocation = () => {
  isLocating.value = true
  if (!navigator.geolocation) {
    errorMessage.value = 'Geolocation is not supported by your browser'
    showError.value = true
    isLocating.value = false
    return
  }
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords
      addMarker([latitude, longitude], "You are here!")
      if (locationMarker) {
        map.removeLayer(locationMarker)
      }
      locationMarker = L.marker([latitude, longitude], {
        icon: L.divIcon({
          className: 'current-location-marker',
          html: '<div class="ping"></div>',
        })
      }).addTo(map)
      map.setView([latitude, longitude], 13)
      isLocating.value = false
    },
    (error) => {
      errorMessage.value = 'Failed to retrieve location'
      showError.value = true
      isLocating.value = false
    }
  )
}

// Route calculation
const calculateRoute = async () => {
  if (!startLocation.value || !endLocation.value) {
    errorMessage.value = 'Please enter both start and end locations'
    showError.value = true
    return
  }

  isCalculatingRoute.value = true
  try {
    const startResponse = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(startLocation.value)}`
    )
    const startData = await startResponse.json()
    const endResponse = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(endLocation.value)}`
    )
    const endData = await endResponse.json()

    if (startData.length === 0 || endData.length === 0) {
      throw new Error('One or both locations not found')
    }

    const startCoords = L.latLng(startData[0].lat, startData[0].lon)
    const endCoords = L.latLng(endData[0].lat, endData[0].lon)

    if (routingControl) {
      map.removeControl(routingControl)
    }

    routingControl = L.Routing.control({
      waypoints: [startCoords, endCoords],
      routeWhileDragging: true,
      showAlternatives: true,
      lineOptions: {
        styles: [{ color: '#1976D2', weight: 4 }]
      }
    }).addTo(map)

    routingControl.on('routesfound', (e) => {
      const routes = e.routes[0]
      routeInfo.value = {
        distance: (routes.summary.totalDistance / 1000).toFixed(2) + ' km',
        duration: Math.round(routes.summary.totalTime / 60) + ' minutes'
      }
    })

    const bounds = L.latLngBounds([startCoords, endCoords])
    map.fitBounds(bounds, { padding: [50, 50] })
  } catch (error) {
    errorMessage.value = error.message || 'Error calculating route'
    showError.value = true
  } finally {
    isCalculatingRoute.value = false
  }
}

const printMap = () => {
  window.print()
}

const clearRoute = () => {
  if (routingControl) {
    map.removeControl(routingControl)
  }
  routeInfo.value = null
}

const resetMap = () => {
  map.setView([23.8859, 45.0792], 5)
  if (currentMarker) {
    map.removeLayer(currentMarker)
  }
  if (locationMarker) {
    map.removeLayer(locationMarker)
  }
  if (routingControl) {
    map.removeControl(routingControl)
  }
  routeInfo.value = null
}

onMounted(() => {
  map = L.map('map', {
    center: [23.8859, 45.0792],
    zoom: 5,
    minZoom: 0,
    maxZoom: 19,
    zoomControl: false,
    maxBounds: [[-90, -180], [90, 180]],
    maxBoundsViscosity: 1.0,
    worldCopyJump: true
  })
  //full screen
  L.Control.Fullscreen = L.Control.extend({
  options: {
    position: 'topleft'
  },

  onAdd: function(map) {
    const container = L.DomUtil.create('div', 'leaflet-control-fullscreen leaflet-bar leaflet-control');
    const button = L.DomUtil.create('a', 'leaflet-control-fullscreen-button', container);
    button.href = '#';
    button.title = 'Full Screen';
    button.innerHTML = '<i class="fas fa-expand"></i>'; // You can replace this with your preferred icon

    L.DomEvent.on(button, 'click', L.DomEvent.stopPropagation)
      .on(button, 'click', L.DomEvent.preventDefault)
      .on(button, 'click', () => {
        toggleFullScreen();
      });

    return container;
  }
});

L.control.fullscreen = function(opts) {
  return new L.Control.Fullscreen(opts);
}

// Add the fullscreen control to the map
L.control.fullscreen({ position: 'topleft' }).addTo(map);

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
  L.GridLayer.prototype._setView = function(center, zoom, noPrune, noUpdate) {
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
  border: 2px solid rgba(0,0,0,0.2);
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
</style>
