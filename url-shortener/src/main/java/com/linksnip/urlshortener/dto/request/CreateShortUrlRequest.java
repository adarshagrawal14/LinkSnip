package com.linksnip.urlshortener.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.validator.constraints.URL;

public record CreateShortUrlRequest(
    @NotBlank @URL @Size(max = 2048) String longUrl,
    @Size(min = 4, max = 20) @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String customAlias,
    @Future Instant expiresAt
) {
}
