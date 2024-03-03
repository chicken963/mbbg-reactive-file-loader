package ru.verstache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.verstache.model.Artist;
import ru.verstache.model.AudioFile;
import ru.verstache.repository.AudioFileRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioFileService {

    private final AudioFileRepository audioFileRepository;
    private final ArtistService artistService;

    public Optional<AudioFile> findById(UUID id) {
        return audioFileRepository.findById(id);
    }


    public Optional<AudioFile> findByArtistAndName(String artist, String name) {
        Optional<Artist> artistFromDb = artistService.findByArtist(artist);
        return artistFromDb.flatMap(value -> this.findByArtistAndName(value, name).stream().findFirst());
    }

    public Optional<AudioFile> findByArtistAndName(Artist artist, String name) {
        return audioFileRepository.findByArtistAndName(artist, name);
    }

    public AudioFile save(AudioFile audioFile) {
        return audioFileRepository.save(audioFile);
    }
}
