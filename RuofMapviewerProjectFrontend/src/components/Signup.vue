<template>
  <div id="signup-container">
    <v-container class="d-flex justify-center align-center fill-height">
      <v-card class="rounded-0 pa-6" width="400" elevation="20" :class="'signup-card'">
        <h2 class="text-center mb-4">Create Account</h2>
        <v-form @submit.prevent="signup">
          <v-text-field 
            prepend-icon="mdi-account-circle-outline" 
            v-model="username" 
            label="Username" 
            required
            density="comfortable"
          ></v-text-field>
          <v-text-field 
            prepend-icon="mdi-email-outline" 
            v-model="email" 
            label="Email" 
            required
            density="comfortable"
          ></v-text-field>
          <v-text-field 
            prepend-icon="mdi-lock-outline" 
            v-model="password" 
            label="Password" 
            :type="showPassword ? 'text' : 'password'"
            required
            @input="checkPasswordRequirements"
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword"
            density="comfortable"
            class="password-field"
          ></v-text-field>
          
          <!-- Password Requirements -->
          <div class="password-requirements mt-2">
            <p class="text-subtitle-2 mb-2">Password Requirements:</p>
            <ul class="requirements-list">
              <li :class="{ 'requirement-met': lengthValid, 'requirement-not-met': !lengthValid }">
                At least 8 characters long
              </li>
              <li :class="{ 'requirement-met': hasLetterAndNumber, 'requirement-not-met': !hasLetterAndNumber }">
                Contains both letters and numbers
              </li>
            </ul>
          </div>

          <v-btn elevation="12" rounded="xl" type="submit" color="blue" block class="mt-4">Sign Up</v-btn>
          <v-alert v-if="error" type="error" class="mt-3">{{ error }}</v-alert>
          <v-alert v-if="message" type="success" class="mt-3">{{ message }}</v-alert>
          <v-btn color="blue" variant="plain" rounded="xl" text class="mt-2" @click="$router.push('/login')">Have an account? Login here</v-btn>
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
export default {
  data() {
    return {
      username: '',
      email: '',
      password: '',
      message: '',
      error: '',
      lengthValid: false,
      hasLetterAndNumber: false,
      showPassword: false
    };
  },
  methods: {
    checkPasswordRequirements() {
      this.lengthValid = this.password.length >= 8;
      this.hasLetterAndNumber = /^(?=.*[a-zA-Z])(?=.*\d).{8,}$/.test(this.password);
    },
    async signup() {
      try {
        const response = await fetch('/req/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username: this.username,
            email: this.email,
            password: this.password,
          }),
        });

        const data = await response.json();

        if (response.ok) {
          this.message = data.message;
          this.error = '';
          this.$router.push('/login');
        } else {
          this.error = data.error || 'Signup failed. Please try again.';
        }
      } catch (err) {
        this.error = 'Something went wrong. Try again.';
      }
    },
  },
};
</script>

<style scoped>
/* Previous styles remain the same */

.password-requirements {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 12px;
  background-color: #f5f5f5;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements-list li {
  margin: 4px 0;
  padding-left: 20px;
  position: relative;
}

.requirements-list li::before {
  content: "•";
  position: absolute;
  left: 5px;
}

.requirement-met {
  color: #4caf50;
}

.requirement-not-met {
  color: #f44336;
}
.password-field :deep(.v-input__append-inner) {
  margin-top: 12px;
  cursor: pointer;
}

.v-text-field :deep(.v-input__append-inner) {
  padding-right: 8px;
}
#signup-container {
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

v-card.signup-card {
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
.v-footer {
  background-color: transparent !important;
}

.v-footer p {
  font-size: 14px;
  margin: 0;
  color: black;
}
</style>


