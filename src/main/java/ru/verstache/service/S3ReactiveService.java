package ru.verstache.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.verstache.exception.DownloadFailedException;
import ru.verstache.model.AudioFile;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ReactiveService {

    private final S3AsyncClient s3AsyncClient;

    @Value("${aws.s3.bucket.audio}")
    private String audioTracksBucket;

    public Mono<ResponseEntity<Flux<ByteBuffer>>> getBinaryContent(AudioFile audioFile) {
        software.amazon.awssdk.services.s3.model.GetObjectRequest request = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(audioTracksBucket)
                .key(audioFile.getS3location())
                .build();

        return Mono.fromFuture(s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
                .map(response -> {
                    log.info("File with id " + audioFile.getId() + " successfully downloaded");
                    checkResult(response.response());
                    String filename = getMetadataItem(response.response(), "filename", audioFile.getId().toString());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment", filename);
                    return ResponseEntity.ok()
                            /* .header(HttpHeaders.CONTENT_TYPE, response.response().contentType())
                             .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.response().contentLength()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")*/
                            .headers(headers)
                            .body(Flux.from(response));
                });
    }

    /**
     * Lookup a metadata key in a case-insensitive way.
     *
     * @param sdkResponse
     * @param key
     * @param defaultValue
     * @return
     */
    private String getMetadataItem(GetObjectResponse sdkResponse, String key, String defaultValue) {
        for (Map.Entry<String, String> entry : sdkResponse.metadata()
                .entrySet()) {
            if (entry.getKey()
                    .equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }

    // Helper used to check return codes from an API call
    private static void checkResult(GetObjectResponse response) {
        SdkHttpResponse sdkResponse = response.sdkHttpResponse();
        if (sdkResponse != null && sdkResponse.isSuccessful()) {
            return;
        }

        throw new DownloadFailedException(response);
    }

    public Mono<byte[]> getByteObject(@NotNull String key) {
        return Mono.just(software.amazon.awssdk.services.s3.model.GetObjectRequest.builder().bucket(audioTracksBucket).key(key).build())
                .map(it -> s3AsyncClient.getObject(it, AsyncResponseTransformer.toBytes()))
                .flatMap(Mono::fromFuture)
                .map(BytesWrapper::asByteArray);
    }
}

