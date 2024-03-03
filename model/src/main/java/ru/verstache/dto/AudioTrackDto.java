package ru.verstache.dto;

import java.util.UUID;

public record AudioTrackDto(UUID id,
                            Double startTime,
                            Double endTime,
                            boolean createdByCurrentUser) {

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof AudioTrackDto audioTrackDto) {
            if (this.hashCode() != audioTrackDto.hashCode()) {
                return false;
            }
            return id.equals(audioTrackDto.id)
                    && startTime.equals(audioTrackDto.startTime)
                    && endTime.equals(audioTrackDto.endTime)
                    && createdByCurrentUser == audioTrackDto.createdByCurrentUser;
        }
        return false;
    }
}
