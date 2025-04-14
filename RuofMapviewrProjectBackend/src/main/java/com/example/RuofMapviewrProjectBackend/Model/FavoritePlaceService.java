package com.example.RuofMapviewrProjectBackend.Model;

import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
@Service
public class FavoritePlaceService {

    @Autowired
    private FavoritePlaceRepository favoritePlaceRepository;

    @Autowired
    private MyAppUserRepository userRepository;

    public FavoritePlace addFavoritePlace(Long userId, FavoritePlace place) {
        MyAppUser user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        place.setUser(user);  // Link the place to the user
        return favoritePlaceRepository.save(place);
    }

    public List<FavoritePlace> getFavoritePlaces(Long userId) {
        MyAppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return favoritePlaceRepository.findByUser(user);
    }

    public void deleteFavoritePlace(Long placeId) {
        favoritePlaceRepository.deleteById(placeId);
    }
}

