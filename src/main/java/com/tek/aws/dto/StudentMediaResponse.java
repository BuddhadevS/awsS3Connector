package com.tek.aws.dto;

public record StudentMediaResponse(
        Long id,
        String name,
        String email,
        String mediaUrl,
        String displayUrl,
        String mediaType,
        String originalFileName
) {
}
