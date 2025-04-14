package com.example.RuofMapviewrProjectBackend.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MyAppUserRepository extends JpaRepository<MyAppUser, Long> {
    Optional<MyAppUser> findByUsername(String username);
    Optional<MyAppUser> findByEmail(String email);
}
