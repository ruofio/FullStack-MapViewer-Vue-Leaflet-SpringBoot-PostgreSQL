package com.example.RuofMapviewrProjectBackend.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {
    List<FavoritePlace> findByUser(MyAppUser user);

}
