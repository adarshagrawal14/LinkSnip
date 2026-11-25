package com.linksnip.urlshortener.dto.response;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds
) {
}
