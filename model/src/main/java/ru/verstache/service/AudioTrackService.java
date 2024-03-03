package ru.verstache.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.verstache.model.AudioTrack;
import ru.verstache.repository.AudioTrackRepository;

import java.util.Optional;
import java.util.UUID;

@Getter
@Service
@RequiredArgsConstructor
public class AudioTrackService  {

    private final AudioTrackRepository audioTrackRepository;

    public Optional<AudioTrack> findById(UUID id) {
        return audioTrackRepository.findById(id);
    }

    public void save(AudioTrack audioTrack) {
        audioTrackRepository.save(audioTrack);
    }

    public void delete(AudioTrack audioTrack) {
        audioTrackRepository.delete(audioTrack);
    }


}
