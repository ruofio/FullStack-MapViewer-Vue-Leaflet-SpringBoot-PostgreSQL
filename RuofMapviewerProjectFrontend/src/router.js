// router.js
import { createRouter, createWebHistory } from 'vue-router'
import Home from './components/HomePage.vue'
import Login from './components/Login.vue'
import Signup from './components/Signup.vue'
import MapCanvas from './components/MapPanel.vue'
import About from './components/About.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/signup', component: Signup },
  { path: '/map', component: MapCanvas },
  {path:'/about',component:About}
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})
let isAuthenticated = false;

router.beforeEach((to, from, next) => {
  if (to.path === '/map' && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export const setLoginStatus = (status) => {
  isAuthenticated = status;
};

export default router
