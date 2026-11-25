package com.linksnip.urlshortener.dto.response;

import java.time.Instant;

public record ShortUrlResponse(
    String shortCode,
    String shortUrl,
    String longUrl,
    Instant createdAt,
    Instant expiresAt
) {
}
