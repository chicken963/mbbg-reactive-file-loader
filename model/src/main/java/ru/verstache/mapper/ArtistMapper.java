package ru.verstache.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.verstache.dto.ArtistDto;
import ru.verstache.model.Artist;
import ru.verstache.service.ArtistService;

@Mapper(componentModel = "spring")
public abstract class ArtistMapper {

    @Autowired
    private ArtistService artistService;

    public Artist toArtist(ArtistDto dto) {
        if (dto.id() != null) {
            return artistService.findById(dto.id()).orElseThrow(() -> new IllegalArgumentException("No artist found by id " + dto.id()));
        }
        return artistService.findArtistOrCreate(dto.artist());
    }

    public Artist toArtist(String artist) {
        return artistService.findArtistOrCreate(artist);
    }

    public ArtistDto toDto(Artist artist) {
        return new ArtistDto(artist.getId(), artist.getArtist());
    }

    public String toStringValue(Artist artist) {
        return this.toDto(artist).artist();
    }
}
