import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import * as components from 'vuetify/components' 
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'
import axios from 'axios';

axios.defaults.baseURL = 'http://localhost:8080/api';
const vuetify = createVuetify({
  components,
  directives,
})


createApp(App)
  .use(vuetify)
  .use(router)
  .mount('#app')
