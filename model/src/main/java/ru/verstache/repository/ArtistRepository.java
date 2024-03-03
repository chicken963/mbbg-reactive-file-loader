package ru.verstache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.verstache.model.Artist;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    Optional<Artist> findByArtist(String artist);

}
