package ru.verstache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.verstache.model.AudioFile;
import ru.verstache.repository.AudioFileRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioFileService {

    private final AudioFileRepository audioFileRepository;

    public Optional<AudioFile> findById(UUID id) {
        return audioFileRepository.findById(id);
    }
}
