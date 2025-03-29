# 🌍 FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL  

## 📌 Project Overview  
**FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL** is a feature-rich interactive mapping application built using **Vue.js, Leaflet.js, Spring Boot, and PostgreSQL**. It enables users to explore maps, track locations, plan routes, estimate distances, and much more.

## ✨ Features  
✅ **User Authentication** – Sign up, log in, and manage user sessions  
✅ **Interactive Map** – Zoom, pan, and explore the map with Leaflet.js  
✅ **Fullscreen Mode** – Expand the map for better visualization  
✅ **Print Map** – Save or print the current map view  
✅ **Get User Location** – Fetch real-time GPS coordinates  
✅ **Reset Map** – Restore the map to its original state  
✅ **Route Planning** – Calculate distance, estimated time, and navigation steps  
✅ **Backend with Spring Boot** – RESTful APIs for data management  
✅ **PostgreSQL Database** – Stores and manages user authentication data  

## 🛠️ Tech Stack  
### Frontend:  
- **Vue.js** – Reactive UI development  
- **Leaflet.js** – Lightweight mapping library  

### Backend:  
- **Spring Boot** – REST API and business logic  
- **PostgreSQL** – Manages user authentication data  

## 🚀 Getting Started  

### 📌 Prerequisites  
Ensure you have the following installed:  
- **Node.js & npm** (for frontend)  
- **Java 17+ & Maven** (for backend)  
- **PostgreSQL** (for user authentication)  

### 🔧 Installation  

#### 1️⃣ Clone the Repository  
```bash
git clone https://github.com/your-username/FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL.git
cd FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL

### 2️⃣ Backend Setup  
Navigate to `backend/src/main/resources/` and create **application.yml**  

Add database credentials, JWT settings, etc.  

Run the backend:  
```bash
cd backend
mvn spring-boot:run

### 3️⃣ Frontend Setup  
Navigate to the frontend folder:  
```bash
cd frontend

Install dependencies and start the app:
```bash
npm install
npm run serve
