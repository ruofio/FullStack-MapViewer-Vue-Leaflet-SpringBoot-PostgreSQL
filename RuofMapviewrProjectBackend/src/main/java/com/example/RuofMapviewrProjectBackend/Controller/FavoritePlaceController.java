package com.example.RuofMapviewrProjectBackend.Controller;

import com.example.RuofMapviewrProjectBackend.Model.FavoritePlace;
import com.example.RuofMapviewrProjectBackend.Model.FavoritePlaceRepository;
import com.example.RuofMapviewrProjectBackend.Model.FavoritePlaceService;
import com.example.RuofMapviewrProjectBackend.Model.MyAppUser;
import com.example.RuofMapviewrProjectBackend.Model.MyAppUserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorite-places")
public class FavoritePlaceController {

    @Autowired
    private FavoritePlaceService favoritePlaceService;

    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private FavoritePlaceRepository favoritePlaceRepository;

    // ✅ Add Favorite Place
    @PostMapping("/add")
    public ResponseEntity<FavoritePlace> addFavoritePlace(
            HttpServletRequest request,
            @RequestBody FavoritePlace place) {

        MyAppUser user = (MyAppUser) request.getSession().getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        place.setUser(user);
        FavoritePlace saved = favoritePlaceRepository.save(place);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ Get All Favorite Places for Logged-in User
    @GetMapping("/my")
    public ResponseEntity<List<FavoritePlace>> getFavoritePlaces(HttpServletRequest request) {
        MyAppUser user = (MyAppUser) request.getSession().getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<FavoritePlace> places = favoritePlaceService.getFavoritePlaces(user.getId());
        return ResponseEntity.ok(places);
    }
    @GetMapping("/api/me")
    public ResponseEntity<MyAppUser> getCurrentUser(HttpServletRequest request) {
        MyAppUser user = (MyAppUser) request.getSession().getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user);
    }
    // ✅ Delete by Place ID
    @DeleteMapping("/delete/{placeId}")
    public ResponseEntity<Void> deleteFavoritePlace(@PathVariable Long placeId) {
        favoritePlaceService.deleteFavoritePlace(placeId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Update a Favorite Place
    @PutMapping("/update/{placeId}")
    public ResponseEntity<FavoritePlace> updateFavoritePlace(@PathVariable Long placeId,
            @RequestBody FavoritePlace updatedData, HttpServletRequest request) {
        
        MyAppUser user = (MyAppUser) request.getSession().getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        Optional<FavoritePlace> existing = favoritePlaceRepository.findById(placeId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        FavoritePlace place = existing.get();
        place.setName(updatedData.getName());
        place.setDescription(updatedData.getDescription());
        place.setLatitude(updatedData.getLatitude());
        place.setLongitude(updatedData.getLongitude());
        place.setUser(user); 
    
        return ResponseEntity.ok(favoritePlaceRepository.save(place));
    }
    
}
