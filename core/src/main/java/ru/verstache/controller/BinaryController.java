package ru.verstache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.verstache.model.Artist;
import ru.verstache.model.AudioFile;
import ru.verstache.model.AudioTrack;
import ru.verstache.model.User;
import ru.verstache.service.*;
import ru.verstache.utils.FileUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/audio-tracks")
@RequiredArgsConstructor
public class BinaryController {

    private final AudioFileService audioFileService;
    private final S3ReactiveService s3ReactiveService;
    private final UserService userService;
    private final ArtistService artistService;
    private final AudioTrackService audioTrackService;

    @GetMapping("/binary/async")
    public Mono<ResponseEntity<byte[]>> getAudioContentReactive(@RequestParam("id") UUID id) {
        Optional<AudioFile> audioFileOpt = audioFileService.findById(id);
        if (audioFileOpt.isPresent()) {
            log.info("found file with id " + id);
            AudioFile audioFile = audioFileOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", String.format("%s.mp3",
                    audioFileOpt.get().getId()));
            return s3ReactiveService.getByteObject(audioFile.getS3location())
                    .map(byteBody -> new ResponseEntity<>(byteBody, headers, HttpStatus.OK));
        } else {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }

    @PostMapping("/add/async")
    public Mono<ResponseEntity<FileResponse>> upload(@RequestParam("artist") String requestedArtist,
                                                     @RequestParam("name") String name,
                                                     @RequestParam("start") Double startTime,
                                                     @RequestParam("end") Double endTime,
                                                     @RequestParam("duration") Double duration,
                                                     @RequestPart("file-data") Mono<FilePart> filePart) {
        Optional<AudioFile> audioFileOpt = audioFileService.findByArtistAndName(requestedArtist, name);
        if (audioFileOpt.isPresent()) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
        }
        User user = userService.getCurrentUser();
        Artist artist = artistService.findArtistOrCreate(requestedArtist);
        //audioFile has id after saving
        AudioFile audioFile = audioFileService.save(AudioFile.builder()
                .artist(artist)
                .name(name)
                .duration(duration)
                .user(user)
                .build());
        AudioTrack audioTrack = AudioTrack.builder()
                .startTime(startTime)
                .endTime(endTime)
                .audioFile(audioFile)
                .user(user)
                .build();
        audioTrackService.save(audioTrack);
        audioFile.setVersions(List.of(audioTrack));

        return filePart
                .map(file -> {
                    FileUtils.filePartValidator(file);
                    return file;
                })
                .flatMap(file -> s3ReactiveService.uploadObject(file, audioFile))
                .map(ResponseEntity::ok);
    }
}
