<template>
  <v-app-bar :theme="currentTheme">
    <v-app-bar-nav-icon @click="drawer = !drawer"></v-app-bar-nav-icon>
    <div class="d-flex align-center app-title-container">
      <div class="app-logo">
        <img src="/logo1.png" alt="Logo" width="190" height="190">
      </div>
    </div>
    <v-spacer></v-spacer>
    <v-btn icon @click="toggleTheme">
      <v-icon>{{ currentTheme === 'dark' ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
    </v-btn>

    <v-btn icon @click="showLogoutDialog = true">
      <v-icon>mdi-logout</v-icon>
    </v-btn>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary>
    <v-list>
      <v-list-item>
        <template v-slot:prepend>
          <v-avatar color="primary">
            <span class="text-h6 white--text">{{ userInitials }}</span>
          </v-avatar>
        </template>
        <v-list-item-title>{{ username }}</v-list-item-title>
        <v-list-item-subtitle>{{ email }}</v-list-item-subtitle>
      </v-list-item>
    </v-list>

    <v-divider></v-divider>

    <div class="pa-4">
      <v-btn block color="error" variant="outlined" @click="showLogoutDialog = true" prepend-icon="mdi-logout">
        Logout
      </v-btn>
    </div>
  </v-navigation-drawer>

  <v-dialog v-model="showLogoutDialog" max-width="400">
    <v-card class="elevation-0" style="background: rgba(255, 255, 255, 0.474) !important; backdrop-filter: blur(10px);">
      <v-card-title class="text-h5">Confirm Logout</v-card-title>
      <v-card-text>
        Are you sure you want to logout?
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey" variant="text" @click="showLogoutDialog = false">Cancel</v-btn>
        <v-btn color="error" variant="text" @click="confirmLogout">Yes, Logout</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useTheme } from 'vuetify';
import { useRouter } from 'vue-router';

const router = useRouter();
const vuetifyTheme = useTheme();
const drawer = ref(false);
const showLogoutDialog = ref(false);
const currentTheme = ref(localStorage.getItem('theme') || 'light');

// User data
const username = ref(localStorage.getItem('username') || 'User');
const email = ref('');
const userInitials = computed(() => {
  return username.value.charAt(0).toUpperCase();
});

const toggleTheme = () => {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  vuetifyTheme.global.name.value = currentTheme.value;
  localStorage.setItem('theme', currentTheme.value);
};

const confirmLogout = async () => {
  try {
    await fetch('/logout', {
      method: 'POST',
      credentials: 'include',
    });
    localStorage.removeItem('username');
    showLogoutDialog.value = false;
    router.push('/');
  } catch (error) {
    console.error('Logout failed:', error);
  }
};
</script>