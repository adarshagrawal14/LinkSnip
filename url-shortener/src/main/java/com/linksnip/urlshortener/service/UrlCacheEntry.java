package com.linksnip.urlshortener.service;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlCacheEntry {
    private Long id;
    private String longUrl;
    private Instant expiresAt;
    private boolean active;
}
