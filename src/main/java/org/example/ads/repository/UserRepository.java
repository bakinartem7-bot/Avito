package org.example.ads.repository;

import org.example.ads.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    Optional<User> findByUsername(@Param("username") String username);
    boolean existsByUsername(@Param("username") String username);
}
