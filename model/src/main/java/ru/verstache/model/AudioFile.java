package ru.verstache.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audiofiles")
public class AudioFile {
    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String s3location;

    @Column(name = "duration")
    private Double duration;

    @ManyToOne
    @JoinColumn(name="created_by", nullable=false)
    private User user;

    @OneToMany(mappedBy = "audioFile", cascade = CascadeType.ALL)
    private List<AudioTrack> versions;

    public static AudioFileBuilder builder() {
        return new AudioFileBuilder();
    }

    public static class AudioFileBuilder {
        private final AudioFile audioFile;

        AudioFileBuilder() {
            this.audioFile = new AudioFile();
        }

        public AudioFileBuilder id(UUID id) {
            this.audioFile.id = id;
            return this;
        }

        public AudioFileBuilder artist(Artist artist) {
            this.audioFile.artist = artist;
            return this;
        }

        public AudioFileBuilder name(String name) {
            this.audioFile.name = name;
            return this;
        }

        public AudioFileBuilder duration(Double duration) {
            this.audioFile.duration = duration;
            return this;
        }

        public AudioFileBuilder user(User user) {
            this.audioFile.user = user;
            return this;
        }

        public AudioFileBuilder versions(List<AudioTrack> audioTracks) {
            this.audioFile.versions = audioTracks;
            return this;
        }

        public AudioFile build() {
            if (this.audioFile.s3location == null) {
                this.audioFile.setS3location(this.audioFile.getS3location());
            }
            if (this.audioFile.versions == null) {
                this.audioFile.versions = new ArrayList<>();
            }
            return this.audioFile;
        }
    }

    public String getS3location() {
        return String.format(String.format("audio/%s/%s", artist.getArtist(), name));
    }
}
