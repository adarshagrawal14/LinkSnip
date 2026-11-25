package com.linksnip.urlshortener.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UrlCacheService {

    private static final String KEY_PREFIX = "urlshortener:url:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, UrlCacheEntry> redisTemplate;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    public UrlCacheService(RedisTemplate<String, UrlCacheEntry> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.cacheHitCounter = Counter.builder("urlshortener.cache.requests")
            .tag("result", "hit")
            .register(meterRegistry);
        this.cacheMissCounter = Counter.builder("urlshortener.cache.requests")
            .tag("result", "miss")
            .register(meterRegistry);
    }

    public Optional<UrlCacheEntry> get(String shortCode) {
        UrlCacheEntry entry = redisTemplate.opsForValue().get(key(shortCode));
        if (entry != null) {
            cacheHitCounter.increment();
        } else {
            cacheMissCounter.increment();
        }
        return Optional.ofNullable(entry);
    }

    public void put(String shortCode, UrlCacheEntry entry) {
        redisTemplate.opsForValue().set(key(shortCode), entry, computeTtl(entry.getExpiresAt()));
    }

    public void evict(String shortCode) {
        redisTemplate.delete(key(shortCode));
    }

    private Duration computeTtl(Instant expiresAt) {
        if (expiresAt == null) {
            return DEFAULT_TTL;
        }
        Duration untilExpiry = Duration.between(Instant.now(), expiresAt);
        if (untilExpiry.isNegative() || untilExpiry.isZero()) {
            return Duration.ofSeconds(1);
        }
        return untilExpiry.compareTo(DEFAULT_TTL) < 0 ? untilExpiry : DEFAULT_TTL;
    }

    private String key(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}
