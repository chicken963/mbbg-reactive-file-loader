package ru.verstache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import ru.verstache.model.AudioFile;
import ru.verstache.service.AudioFileService;
import ru.verstache.service.S3ReactiveService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BinaryController {

    private final AudioFileService audioFileService;
    private final S3ReactiveService s3ReactiveService;

    @GetMapping("/binary/async")
    public Mono<ResponseEntity<byte[]>> getAudioContentReactive(@RequestParam("id") UUID id) {
        Optional<AudioFile> audioFileOpt = audioFileService.findById(id);
        if (audioFileOpt.isPresent()) {
            log.info("found file with id " + id);
            AudioFile audioFile = audioFileService.findById(id).get();
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
}
