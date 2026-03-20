package com.tek.aws.dto;

public record MediaUploadResponse(
        Long studentId,
        String name,
        String email,
        String mediaUrl,
        String displayUrl,
        String mediaType,
        String message
) {
}
