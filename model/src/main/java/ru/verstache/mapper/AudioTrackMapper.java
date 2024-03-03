package ru.verstache.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.verstache.dto.AudioTrackDto;
import ru.verstache.model.AudioTrack;
import ru.verstache.service.UserService;

@Mapper(componentModel = "spring")
public abstract class AudioTrackMapper {

    @Autowired
    private UserService userService;

    public AudioTrack toAudioTrack(AudioTrackDto audioTrackDto) {
        return AudioTrack.builder()
                .id(audioTrackDto.id())
                .startTime(audioTrackDto.startTime())
                .endTime(audioTrackDto.endTime())
                .build();
    }

    public AudioTrackDto toAudioTrackDto(AudioTrack audioTrack) {
        return new AudioTrackDto(
                audioTrack.getId(),
                audioTrack.getStartTime(),
                audioTrack.getEndTime(),
                true
        );
    }
}
