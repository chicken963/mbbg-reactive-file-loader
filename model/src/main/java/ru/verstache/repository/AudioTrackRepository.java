package ru.verstache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.verstache.model.AudioTrack;

import java.util.UUID;

public interface AudioTrackRepository extends JpaRepository<AudioTrack, UUID> {

}
