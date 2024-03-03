package ru.verstache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.verstache.model.Artist;
import ru.verstache.model.AudioFile;

import java.util.Optional;
import java.util.UUID;

public interface AudioFileRepository  extends JpaRepository<AudioFile, UUID> {
    Optional<AudioFile> findByArtistAndName(Artist artist, String name);
}
