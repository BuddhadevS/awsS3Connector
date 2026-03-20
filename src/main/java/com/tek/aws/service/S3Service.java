package com.tek.aws.service;

import com.tek.aws.exception.MediaUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.bucketName}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    @Value("${presign.expiry}")
    private long presignExpiryMinutes;

    @Value("${app.media.max-file-size-bytes}")
    private long maxFileSize;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/quicktime",
            "video/webm"
    );

    public UploadResult uploadMedia(MultipartFile file) {
        validateFile(file);
        String fileKey = buildFileKey(file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return new UploadResult(
                    fileKey,
                    buildMediaUrl(fileKey),
                    file.getContentType(),
                    file.getOriginalFilename()
            );

        } catch (Exception e) {
            throw new MediaUploadException("Failed to upload file to S3", e);
        }
    }

    public String createPresignedGetUrl(String fileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpiryMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaUploadException("File is required");
        }

        if (file.getSize() > maxFileSize) {
            throw new MediaUploadException("File size exceeds the configured limit");
        }

        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new MediaUploadException("Only image and mp4/webm/mov video files are allowed");
        }
    }

    private String buildFileKey(String originalFilename) {
        String safeFilename = originalFilename == null
                ? "media-file"
                : URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
        return "students/%s_%s".formatted(UUID.randomUUID(), safeFilename);
    }

    private String buildMediaUrl(String fileKey) {
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, fileKey);
    }

    public record UploadResult(
            String fileKey,
            String mediaUrl,
            String mediaType,
            String originalFileName
    ) {
    }
}
