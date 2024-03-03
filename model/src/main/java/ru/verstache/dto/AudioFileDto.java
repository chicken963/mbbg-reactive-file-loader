package ru.verstache.dto;

import java.util.List;
import java.util.UUID;

public record AudioFileDto(UUID id,
                           String artist,
                           String name,
                           String s3location,
                           Double length,
                           boolean createdByCurrentUser,
                           List<AudioTrackDto> versions) {

}
