package ru.verstache.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.verstache.dto.AudioFileDto;
import ru.verstache.model.AudioFile;
import ru.verstache.service.UserService;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AudioFileMapper {

    @Autowired
    private UserService userService;

    @Autowired
    private ArtistMapper artistMapper;

    @Autowired
    private AudioTrackMapper audioTrackMapper;

    public AudioFile toAudioFile(AudioFileDto audioFileDto) {
        return AudioFile.builder()
                .id(audioFileDto.id())
                .artist(artistMapper.toArtist(audioFileDto.artist()))
                .name(audioFileDto.name())
                .duration(audioFileDto.length())
                .versions(audioFileDto.versions().stream()
                        .map(audioTrackMapper::toAudioTrack)
                        .collect(Collectors.toList()))
                .build();
    }

    public AudioFileDto toDto(AudioFile audioFile) {
        return new AudioFileDto(
                audioFile.getId(),
                artistMapper.toStringValue(audioFile.getArtist()),
                audioFile.getName(),
                audioFile.getS3location(),
                audioFile.getDuration(),
                true,
                audioFile.getVersions().stream()
                        .map(audioTrackMapper::toAudioTrackDto)
                        .collect(Collectors.toList())
        );
    }
}
