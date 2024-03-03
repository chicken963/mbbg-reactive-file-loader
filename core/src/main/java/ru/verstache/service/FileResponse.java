package ru.verstache.service;

import ru.verstache.dto.AudioFileDto;

public record FileResponse(AudioFileDto audioFile, String uploadId, String path, String type, String eTag) {}
