<template>
  <div id="login-container">
    <v-container class="d-flex justify-center align-center fill-height">
      <v-card class="rounded-0 pa-6" width="400" elevation="20" :class="'login-card'">
        <h2 class="text-center mb-4">Login to Your Account</h2>
        <v-form @submit.prevent="login">
          <v-text-field 
            prepend-icon="mdi-account-circle-outline" 
            v-model="username" 
            label="Username" 
            required
            density="comfortable"
          ></v-text-field>
          <v-text-field 
            prepend-icon="mdi-lock-outline" 
            v-model="password" 
            label="Password" 
            :type="showPassword ? 'text' : 'password'"
            required
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword"
            density="comfortable"
            class="password-field"
          ></v-text-field>
          <v-btn elevation="12" rounded="xl" type="submit" color="blue" block>Login</v-btn>
          <v-alert v-if="error" type="error" class="mt-3">{{ error }}</v-alert>
          <v-btn color="blue" variant="plain" rounded="xl" text class="mt-2" @click="$router.push('/signup')">Create a new account</v-btn>
        </v-form>
        <v-btn color="grey" variant="outlined" class="mt-4" @click="$router.push('/')">Back to Home</v-btn>
      </v-card>
    </v-container>
  </div>
  <v-footer class="pa-4">
    <v-row justify="center">
      <v-col cols="12" md="6" class="text-center text-black">
        <p>&copy; 2025 MapViewer. All rights reserved.</p>
      </v-col>
    </v-row>
  </v-footer>
</template>
<script>
  import { setLoginStatus } from '../router';
  
  export default {
    data() {
    return {
      username: '',
      password: '',
      error: '',
      showPassword: false
    };
    },
    methods: {
      async login() {
        try {
          const response = await fetch('/login', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
              username: this.username,
              password: this.password,
            }),
            credentials: 'include',
          });
  
          if (response.ok) {
            setLoginStatus(true);
            this.$router.push('/map');
            localStorage.setItem('username', this.username);
          } else {
            this.error = 'Invalid username or password';
          }
        } catch (err) {
          this.error = 'Login failed. Try again later.';
        }
      },
    },
  };
  </script>

<style scoped>
#login-container {
  background-color: #f0f7ff; /* Lighter blue background */
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

v-card.login-card {
  background: #ffffff;
  border: 1px solid #cce0ff; /* Lighter border color */
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  border-radius: 15px; /* Rounded corners for a softer look */
}

h2 {
  color: #1e88e5; /* Blue color for heading */
  font-family: 'Poppins', sans-serif; /* Modern font */
  font-weight: 600; /* Make the heading bolder */
}

v-btn {
  text-transform: none; /* Prevent text from being capitalized */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15); /* Button shadow */
}

v-btn:hover {
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.2); /* Stronger hover shadow */
}

.password-field :deep(.v-input__append-inner) {
  margin-top: 12px;
  cursor: pointer;
}

.v-text-field :deep(.v-input__append-inner) {
  padding-right: 8px;
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
