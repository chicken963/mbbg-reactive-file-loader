package ru.verstache.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "artists")
public class Artist {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "artist")
    private String artist;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    private List<AudioFile> audioFiles;

}
