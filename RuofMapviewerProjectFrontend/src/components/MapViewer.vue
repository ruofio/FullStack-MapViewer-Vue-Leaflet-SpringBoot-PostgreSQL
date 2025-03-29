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
      <v-btn icon class="fullscreen-toggle" @click="toggleFullScreen">
        <v-icon>
          {{ isFullScreen ? 'mdi-fullscreen-exit' : 'mdi-fullscreen' }}
        </v-icon>
      </v-btn>

      <v-row>
        <v-col cols="12" sm="6" md="4">
          <v-text-field v-model="searchQuery" label="Search location" prepend-inner-icon="mdi-magnify" clearable
            @keyup="onSearchInput" @click:clear="clearSearchResults"  class="search-bar">
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

      <!-- Map Container -->
      <div id="map"></div>

      <!-- Routing Panel -->
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

            <!-- Route Information -->
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

      <!-- Map Control Buttons -->
      <div class="map-controls">
        <v-btn color="primary" class="map-btn" prepend-icon="mdi-crosshairs-gps" @click="getCurrentLocation"
          :loading="isLocating">
          My Location
        </v-btn>
        <v-btn color="secondary" class="map-btn" prepend-icon="mdi-printer" @click="printMap">
          Print Map
        </v-btn>
        <v-btn color="primary" class="map-btn" prepend-icon="mdi-refresh" @click="resetMap">
          Reset Map
        </v-btn>
      </div>
    </v-card-text>

    <!-- Snackbars -->
    <v-snackbar v-model="showError" color="error" timeout="3000">
      {{ errorMessage }}
    </v-snackbar>

    <v-snackbar v-model="showSuccess" color="success" timeout="3000">
      {{ successMessage }}
    </v-snackbar>

    <!-- Footer Section -->
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

// State variables
const searchQuery = ref('')
const searchResults = ref([]) // Holds search suggestions
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
const showSuggestions = ref(true);



// Map and marker variables
let map = null
let currentMarker = null
let locationMarker = null
let routingControl = null

// Full-screen toggle method
const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value

  // Browser full-screen handling
  if (!document.fullscreenElement) {
    const mapContainer = document.querySelector('.map-container')
    if (mapContainer.requestFullscreen) {
      mapContainer.requestFullscreen()
    } else if (mapContainer.mozRequestFullScreen) {
      mapContainer.mozRequestFullScreen()
    } else if (mapContainer.webkitRequestFullscreen) {
      mapContainer.webkitRequestFullscreen()
    } else if (mapContainer.msRequestFullscreen) {
      mapContainer.msRequestFullscreen()
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
  }

  // Resize map after a short delay
  setTimeout(() => {
    if (map) {
      map.invalidateSize()
    }
  }, 100)
}

// Fullscreen change event handler
const handleFullscreenChange = () => {
  isFullScreen.value = !!document.fullscreenElement
}
// Search location function
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

// Handle search input to show suggestions
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

// Select location from suggestions
const selectLocation = (location) => {
  searchQuery.value = location.display_name
  searchResults.value = [] // Clear the suggestions

  const coords = L.latLng(location.lat, location.lon)
  map.setView(coords, 13)
  addMarker(coords, `Location: ${location.display_name}`)
}


const clearSearchResults = () => {
  searchQuery.value = ''
  searchResults.value = [] // Clears the suggestions list
}


// Add marker function
const addMarker = (coords, description) => {
  if (currentMarker) {
    map.removeLayer(currentMarker)
  }

  currentMarker = L.marker(coords)
    .addTo(map)
    .bindPopup(description)
    .openPopup()
}

// Get current location function
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

      // Add the "You are here!" marker
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

// Calculate route function

const calculateRoute = async () => {

  if (!startLocation.value || !endLocation.value) {

    errorMessage.value = 'Please enter both start and end locations'

    showError.value = true

    return

  }

  isCalculatingRoute.value = true

  try {

    // Get coordinates for start location

    const startResponse = await fetch(

      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(startLocation.value)}`

    )

    const startData = await startResponse.json()

    // Get coordinates for end location

    const endResponse = await fetch(

      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(endLocation.value)}`

    )

    const endData = await endResponse.json()

    if (startData.length === 0 || endData.length === 0) {

      throw new Error('One or both locations not found')

    }

    const startCoords = L.latLng(startData[0].lat, startData[0].lon)

    const endCoords = L.latLng(endData[0].lat, endData[0].lon)

    // Remove existing routing control if it exists

    if (routingControl) {

      map.removeControl(routingControl)

    }

    // Add new routing control

    routingControl = L.Routing.control({

      waypoints: [startCoords, endCoords],

      routeWhileDragging: true,

      showAlternatives: true,

      lineOptions: {

        styles: [{ color: '#1976D2', weight: 4 }]

      }

    }).addTo(map)

    // Update route info when route is calculated

    routingControl.on('routesfound', (e) => {

      const routes = e.routes[0]

      routeInfo.value = {

        distance: (routes.summary.totalDistance / 1000).toFixed(2) + ' km',

        duration: Math.round(routes.summary.totalTime / 60) + ' minutes'

      }

    })

    // Fit the map to show the entire route

    const bounds = L.latLngBounds([startCoords, endCoords])

    map.fitBounds(bounds, { padding: [50, 50] })

  } catch (error) {

    errorMessage.value = error.message || 'Error calculating route'

    showError.value = true

  } finally {

    isCalculatingRoute.value = false

  }

}

// Print map function
const printMap = () => {
  window.print()
}


// Clear route function
const clearRoute = () => {
  if (routingControl) {
    map.removeControl(routingControl)
  }
  routeInfo.value = null
}

// Reset map function
const resetMap = () => {
  map.setView([23.8859, 45.0792], 5) // Center map to Saudi Arabia
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

// Initialize map on mount
onMounted(() => {
  map = L.map('map').setView([23.8859, 45.0792], 5) // Center map to Saudi Arabia

  L.tileLayer(
    'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }
  ).addTo(map)

  // Add fullscreen event listeners
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.addEventListener('mozfullscreenchange', handleFullscreenChange)
  document.addEventListener('MSFullscreenChange', handleFullscreenChange)
})

// Remove event listeners when component is unmounted
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
  /* Adjust as needed */
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
  height: 600px;
  width: 100%;
  transition: height 0.3s ease;
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
    margin-bottom: 4px; /* Adjust this value to control the space */
  }
.search-button {
  font-size: 16px; /* Makes the text larger */
  padding: 10px 22px; /* Increases padding for a bigger button */
  min-width: 120px; /* Ensures a minimum width */
}
.suggestions-list {
  margin-top: -28px; /* Adjust to remove space */
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

@media print {

  .v-card-title,
  .v-text-field,
  .map-controls,
  .v-snackbar,
  .v-expansion-panels {
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
</style>
