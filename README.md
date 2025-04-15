# 🌍 Map Viewer Application

An interactive map viewer built with **Vue.js, Leaflet, Spring Boot, and PostgreSQL**, offering various geospatial features such as location tracking, route navigation, fullscreen mode, printing, and resetting to the original state.

## 🚀 Features

- 🏠 **Home & About Pages**  
- 🔑 **User Authentication** (Login & Signup)  
- 🗺 **Interactive Map Display** (Leaflet-based)  
- 📌 **Show My Location**  
- ✏️ **Draw on the Map**  
  - Draw shapes like polygons and lines  
  - Cancel drawing at any time  
- 🌐 **Change Map Language**  
  - Supports multiple languages  
  - Map and UI update dynamically  
- 💙 **Favorite Places Management**  
  - Add, update, and delete favorite places  
  - Displayed on map as blue heart icons  
  - Info window displays place details  
  - Persisted for each user across sessions  
  - Each user sees only their own favorites  
- 📍 **Custom Marker with Place Info**  
  - Mark places and view info on click  
- 📏 **Routing & Navigation**  
  - Calculate distance and time  
  - Step-by-step route instructions  
- 🔄 **Reset Map to Original View**  
- 🖥 **Fullscreen Mode**  
- 🖨 **Print Map**

## 🛠 Tech Stack

- **Frontend**: Vue.js, Leaflet  
- **Backend**: Spring Boot  
- **Database**: PostgreSQL  
- **API & Services**: OpenStreetMap (Leaflet Routing Machine)  

## 📦 Installation

### 1️⃣ Clone the Repository
```sh
git clone https://github.com/your-username/FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL.git
cd FullStack-MapViewer-Vue-Leaflet-SpringBoot-PostgreSQL
```
### 2️⃣ Backend Setup  
#### Make sure you have Java 17+ and Maven installed.

Navigate to `backend/src/main/resources/` and create **application.yml** 
Add database credentials, JWT settings, etc.

### 3️⃣ Frontend Setup
#### Ensure Node.js (v16+) is installed
Navigate to the frontend folder:
```sh
cd frontend
```

Install dependencies and start the app:
```sh
npm install
npm run serve
```


### 4️⃣ Access the Application
Now, open your browser and go to `http://localhost:5173/` to view the application.

---
## 📸 Screenshot of the Website

![Image](https://github.com/user-attachments/assets/f03a9f36-e0e4-4c29-a5c0-b6bbeb348906)

![Image](https://github.com/user-attachments/assets/f2443981-470a-4bdc-8831-e653eacb4fb6)
