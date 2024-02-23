package ru.verstache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.verstache.model.AudioFile;

import java.util.UUID;

public interface AudioFileRepository  extends JpaRepository<AudioFile, UUID> {

}
