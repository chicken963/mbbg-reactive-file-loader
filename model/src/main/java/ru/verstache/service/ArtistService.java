package ru.verstache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.verstache.model.Artist;
import ru.verstache.model.AudioFile;
import ru.verstache.repository.ArtistRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    
    public Optional<Artist> findByArtist(String artist) {
        return artistRepository.findByArtist(artist);
    }

    
    public Artist findArtistOrCreate(String artist) {
        Optional<Artist> artistFromDb = findByArtist(artist);
        return artistFromDb.orElseGet(
                () -> artistRepository.save(
                        Artist.builder()
                                .artist(artist)
                                .build())
        );
    }

    
    public void removeArtist(Artist artist) {
        artistRepository.delete(artist);
    }

    
    public Optional<Artist> findById(UUID id) {
        return artistRepository.findById(id);
    }

    
    public void updateArtistIfNeeded(AudioFile audioFile, Artist newArtist) {
        if (!audioFile.getArtist().equals(newArtist)) {
            Artist oldArtist = audioFile.getArtist();
            audioFile.setArtist(newArtist);
            oldArtist.getAudioFiles().remove(audioFile);
            if (oldArtist.getAudioFiles().isEmpty()) {
                removeArtist(oldArtist);
            }
        }
    }
}