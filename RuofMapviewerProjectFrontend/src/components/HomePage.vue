<template>
  <v-app>
    <v-main class="home-page">
      <!-- Navigation Bar -->
      <v-app-bar flat color="transparent" elevate-on-scroll class="px-4">
        <v-img src="/logo1.png" max-height="190" max-width="190" class="mt-2 mr-4"></v-img>
        <v-spacer></v-spacer>
        <v-btn text class="rounded-pill mx-2" @click="$router.push('/')">Home</v-btn>
        <v-btn text class="rounded-pill mx-2" @click="$router.push('/about')">About</v-btn>
        <v-btn text class="rounded-pill mx-2" @click="$router.push('/login')" :class="['rounded-pill', 'dark-blue-btn']">Login</v-btn>
        <v-btn color="blue darken-2" class="rounded-pill mx-2" @click="$router.push('/signup')" :class="['rounded-pill', 'dark-blue-btn']">
          Join Now
        </v-btn>
      </v-app-bar>

      <!-- Hero Section -->
      <v-container class="py-12">
        <v-row justify="center" class="mb-16">
          <v-col cols="12" md="8" class="mt-15 text-center">
            <h1 class="text-h2 font-weight-bold mb-6 gradient-text">
              Welcome to MapViewer! <br>
              Navigate Your World with Ease
            </h1>

            <p class="text-h6 font-weight-regular text-grey-darken-1 mb-8">
              Plan routes, explore maps, and experience seamless navigation like never before.
            </p>
            <v-btn size="x-large" color="blue-darken-2" class="rounded-pill px-8 py-3 explore-btn" elevation="3"
              @click="$router.push('/signup')">
              Explore Now
              <v-icon right class="ml-2">mdi-directions</v-icon>
            </v-btn>
          </v-col>
        </v-row>

       <!-- Why Choose MapViewer Section -->
       <v-row justify="center" class="features-section">
          <v-col cols="12" class="text-center mb-12">
            <v-chip color="blue" class="mb-4" label>Features</v-chip>
            <h2 class="text-h3 font-weight-bold mb-3">Why Choose MapViewer?</h2>
          </v-col>

          <v-col cols="12">
            <v-row justify="center">
              <v-col cols="12" md="6" lg="3" v-for="(feature, index) in features" :key="index">
                <v-card elevation="0" class="feature-card-new">
                  <div class="feature-icon-wrapper mb-6">
                    <v-icon size="32" :color="feature.color">{{ feature.icon }}</v-icon>
                  </div>
                  <h3 class="text-h5 font-weight-bold mb-4">{{ feature.title }}</h3>
                  <p class="text-body-1 text-grey-darken-1">{{ feature.description }}</p>
                  <v-btn variant="text" color="blue-darken-2" class="mt-4" @click="openFeatureModal(feature)">
                    Learn More
                    <v-icon right>mdi-arrow-right</v-icon>
                  </v-btn>
                </v-card>
              </v-col>
            </v-row>
          </v-col>
        </v-row>
        <!-- Feature Modal -->
        <v-dialog v-model="showFeatureModal" max-width="600">
          <v-card v-if="selectedFeature">
            <v-card-title class="text-h4 pa-6">
              {{ selectedFeature.title }}
              <v-spacer></v-spacer>
              <v-btn icon @click="showFeatureModal = false">
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </v-card-title>
            <v-card-text class="pa-6">
              <div class="feature-icon-wrapper mb-6"
                :style="{ background: `linear-gradient(135deg, ${selectedFeature.gradientColors[0]} 0%, ${selectedFeature.gradientColors[1]} 100%)` }">
                <v-icon size="32" :color="selectedFeature.color">{{ selectedFeature.icon }}</v-icon>
              </div>
              <p class="mb-6">{{ selectedFeature.fullDescription }}</p>
              <v-list v-if="selectedFeature.benefits">
                <v-list-subheader class="text-h6">Key Benefits</v-list-subheader>
                <v-list-item v-for="(benefit, i) in selectedFeature.benefits" :key="i">
                  <template v-slot:prepend>
                    <v-icon color="blue-darken-2">mdi-check-circle</v-icon>
                  </template>
                  <v-list-item-title>{{ benefit }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-dialog>
      </v-container>

      <!-- Footer Section -->
      <v-footer class="pa-4">
        <v-row justify="center">
          <v-col cols="12" md="6" class="text-center text-black">
            <p>&copy; 2025 MapViewer. All rights reserved.</p>
          </v-col>
        </v-row>
      </v-footer>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from 'vue';
const showFeatureModal = ref(false);
const selectedFeature = ref(null);
const features = [
  { 
    icon: 'mdi-map-marker-path', 
    title: 'Smart Routing', 
    description: 'Find the most efficient paths with real-time optimization.',
    color: 'blue-darken-2',
    gradientColors: ['#E3F2FD', '#BBDEFB'],
    fullDescription: 'Our intelligent routing system enables users to navigate efficiently by calculating the optimal path between two points. It provides essential details such as travel duration, distance, and step-by-step route instructions.',
    benefits: [
      'Accurate distance and duration estimation',
      'Optimized routing based on available paths',
      'Estimated time of arrival predictions',
      'Step-by-step navigation instructions'
    ]
  },
  { 
    icon: 'mdi-map-check', 
    title: 'Interactive Mapping', 
    description: 'Explore, locate, search, and customize maps with ease.',
    color: 'blue-darken-1',
    gradientColors: ['#E1F5FE', '#B3E5FC'],
    fullDescription: 'COur map viewer allows users to explore a world map, detect their current location, and search for specific places. Users can also print maps and interact with key locations using customizable tools',
    benefits: [
      'Real-time location detection',
      'Search functionality for places and landmarks',
      'Route visualization for better navigation',
      'Map printing capabilities'
    ]
  },
  { 
    icon: 'mdi-human-greeting-variant', 
    title: 'User Experience', 
    description: 'An intuitive interface designed for smooth navigation.',
    color: 'blue',
    gradientColors: ['#E8EAF6', '#C5CAE9'],
    fullDescription: 'Our user interface is designed with simplicity and efficiency in mind, making it easy for anyone to navigate and use our advanced features.',
    benefits: [
      'Intuitive controls and gestures',
      'Customizable interface',
      'Quick access to frequently used features',
      'Responsive design for all devices'
    ]
  },
  { 
    icon: 'mdi-shield-lock-outline', 
    title: 'Advanced Security', 
    description: 'Your data is protected with encryption.',
    color: 'blue-lighten-1',
    gradientColors: ['#F3E5F5', '#E1BEE7'],
    fullDescription: 'We implement state-of-the-art security measures to protect your data and ensure your privacy while using our platform.',
    benefits: [
      'End-to-end encryption',
      'Secure data storage',
      'Privacy controls',
      'Regular security audits'
    ]
  }
];

const openFeatureModal = (feature) => {
  selectedFeature.value = feature;
  showFeatureModal.value = true;
};

</script>

<style scoped>
.home-page {
  background: linear-gradient(45deg, #6eb1e3, #d0e7f9, #ffffff);
  background-size: 600% 600%;
  animation: gradientAnimation 15s ease infinite;
}

@keyframes gradientAnimation {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}

.gradient-text {
  background: linear-gradient(45deg, #2094ee, #1677c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.feature-card-new {
  padding: 2rem;
  height: 100%;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.feature-card-new:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
}

.feature-icon-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.explore-btn {
  transition: background 0.3s ease-in-out;
}

.explore-btn:hover {
  background-color: #1677c2 !important;
}

.dark-blue-btn {
  background-color: #1677c2 !important;
  color: white;
}

.v-footer {
  background-color: transparent !important;
}

.v-footer p {
  font-size: 14px;
  margin: 0;
  color: black;
}
</style>

