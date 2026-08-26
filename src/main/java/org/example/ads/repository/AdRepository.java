package org.example.ads.repository;

import org.example.ads.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AdRepository extends JpaRepository<Ad, UUID> {
    List<Ad> findAllByAuthorId(UUID authorId);
}
