package ru.verstache.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audiotracks")
public class AudioTrack {

    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time")
    private Double startTime;

    @Column(name = "end_time")
    private Double endTime;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name="file_id", nullable=false)
    private AudioFile audioFile;

    @ManyToOne
    @JoinColumn(name="created_by", nullable=false)
    private User user;

    public boolean hasBounds(Double startTime, Double endTime) {
        return Math.abs(this.startTime - startTime) < 1 && Math.abs(this.endTime - endTime) < 1;
    }
}
