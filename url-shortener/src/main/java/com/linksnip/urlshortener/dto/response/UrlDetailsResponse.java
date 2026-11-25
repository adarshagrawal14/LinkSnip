package com.linksnip.urlshortener.dto.response;

import java.time.Instant;

public record UrlDetailsResponse(
    String shortCode,
    String shortUrl,
    String longUrl,
    Instant createdAt,
    Instant expiresAt,
    long clickCount,
    boolean active
) {
}
