package ru.verstache.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.verstache.exception.DownloadFailedException;
import ru.verstache.mapper.AudioFileMapper;
import ru.verstache.model.AudioFile;
import ru.verstache.model.UploadStatus;
import ru.verstache.utils.FileUtils;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ReactiveService {

    private final S3AsyncClient s3AsyncClient;
    private final AudioFileMapper audioFileMapper;

    @Value("${aws.s3.bucket.audio}")
    private String audioTracksBucket;

    @Value("${aws.multipart.min.part.size}")
    private Integer multipartMinPartSize;

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

    public Mono<FileResponse> uploadObject(FilePart filePart, AudioFile audioFile) {

        String s3Location = audioFile.getS3location();
        String filename = filePart.filename();

        Map<String, String> metadata = Map.of("filename", filename);
        MediaType mediaType = ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);

        CompletableFuture<CreateMultipartUploadResponse> s3AsyncClientMultipartUpload = s3AsyncClient
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mediaType.toString())
                        .key(s3Location)
                        .metadata(metadata)
                        .bucket(audioTracksBucket)
                        .build());

        UploadStatus uploadStatus = new UploadStatus(Objects.requireNonNull(filePart.headers().getContentType()).toString(), s3Location);

        return Mono.fromFuture(s3AsyncClientMultipartUpload)
                .flatMapMany(response -> {
                    FileUtils.checkSdkResponse(response);
                    uploadStatus.setUploadId(response.uploadId());
                    log.info("Upload object with ID={}", response.uploadId());
                    return filePart.content();
                })
                .bufferUntil(dataBuffer -> {
                    uploadStatus.addBuffered(dataBuffer.readableByteCount());

                    if (uploadStatus.getBuffered() >= multipartMinPartSize) {
                        log.info("BufferUntil - returning true, bufferedBytes={}, partCounter={}, uploadId={}",
                                uploadStatus.getBuffered(), uploadStatus.getPartCounter(), uploadStatus.getUploadId());
                        uploadStatus.setBuffered(0);
                        return true;
                    }

                    return false;
                })
                .map(FileUtils::dataBufferToByteBuffer)
                .flatMap(byteBuffer -> uploadPartObject(uploadStatus, byteBuffer))
                .onBackpressureBuffer()
                .reduce(uploadStatus, (status, completedPart) -> {
                    log.info("Completed: PartNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    (status).getCompletedParts().put(completedPart.partNumber(), completedPart);
                    return status;
                })
                .flatMap(uploadStatus1 -> completeMultipartUpload(uploadStatus))
                .map(response -> {
                    FileUtils.checkSdkResponse(response);
                    log.info("upload result: {}", response.toString());
                    return new FileResponse(audioFileMapper.toDto(audioFile), uploadStatus.getUploadId(), response.location(), uploadStatus.getContentType(), response.eTag());
                });
    }

    private Mono<CompletedPart> uploadPartObject(UploadStatus uploadStatus, ByteBuffer buffer) {
        final int partNumber = uploadStatus.getAddedPartCounter();
        log.info("UploadPart - partNumber={}, contentLength={}", partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> uploadPartResponseCompletableFuture = s3AsyncClient.uploadPart(UploadPartRequest.builder()
                        .bucket(audioTracksBucket)
                        .key(uploadStatus.getFileKey())
                        .partNumber(partNumber)
                        .uploadId(uploadStatus.getUploadId())
                        .contentLength((long) buffer.capacity())
                        .build(),
                AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono.fromFuture(uploadPartResponseCompletableFuture)
                .map(uploadPartResult -> {
                    FileUtils.checkSdkResponse(uploadPartResult);
                    log.info("UploadPart - complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                    return CompletedPart.builder()
                            .eTag(uploadPartResult.eTag())
                            .partNumber(partNumber)
                            .build();
                });
    }

    /**
     * This method is called when a part finishes uploading. It's primary function is to verify the ETag of the part
     * we just uploaded.
     */
    private Mono<CompleteMultipartUploadResponse> completeMultipartUpload(UploadStatus uploadStatus) {
        log.info("CompleteUpload - fileKey={}, completedParts.size={}",
                uploadStatus.getFileKey(), uploadStatus.getCompletedParts().size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(uploadStatus.getCompletedParts().values())
                .build();

        return Mono.fromFuture(s3AsyncClient.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(audioTracksBucket)
                .uploadId(uploadStatus.getUploadId())
                .multipartUpload(multipartUpload)
                .key(uploadStatus.getFileKey())
                .build()));
    }
}

