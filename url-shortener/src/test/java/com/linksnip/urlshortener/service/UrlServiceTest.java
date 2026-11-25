package com.linksnip.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.linksnip.urlshortener.dto.request.CreateShortUrlRequest;
import com.linksnip.urlshortener.entity.Url;
import com.linksnip.urlshortener.exception.AliasAlreadyExistsException;
import com.linksnip.urlshortener.exception.ReservedAliasException;
import com.linksnip.urlshortener.exception.UrlExpiredException;
import com.linksnip.urlshortener.exception.UrlNotFoundException;
import com.linksnip.urlshortener.repository.UrlRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;
    @Mock
    private ShortCodeGeneratorService shortCodeGeneratorService;
    @Mock
    private UrlCacheService urlCacheService;
    @Mock
    private ClickTrackingService clickTrackingService;

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(
            urlRepository, shortCodeGeneratorService, urlCacheService, clickTrackingService, "http://localhost:8080"
        );
    }

    @Test
    void rejectsReservedAlias() {
        var request = new CreateShortUrlRequest("https://example.com", "admin", null);

        assertThatThrownBy(() -> urlService.createShortUrl(request, null))
            .isInstanceOf(ReservedAliasException.class);
    }

    @Test
    void rejectsDuplicateAlias() {
        var request = new CreateShortUrlRequest("https://example.com", "taken", null);
        when(urlRepository.existsByShortCode("taken")).thenReturn(true);

        assertThatThrownBy(() -> urlService.createShortUrl(request, null))
            .isInstanceOf(AliasAlreadyExistsException.class);
    }

    @Test
    void resolvesActiveNonExpiredLinkOnCacheMiss() {
        Url url = Url.builder()
            .id(1000L).shortCode("abc").longUrl("https://example.com")
            .active(true).createdAt(Instant.now()).build();
        when(urlCacheService.get("abc")).thenReturn(Optional.empty());
        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));

        String result = urlService.resolveLongUrl("abc", null, null, "127.0.0.1");

        assertThat(result).isEqualTo("https://example.com");
    }

    @Test
    void throwsNotFoundForInactiveCachedLink() {
        UrlCacheEntry inactiveEntry = UrlCacheEntry.builder()
            .id(1L).longUrl("https://example.com").active(false).build();
        when(urlCacheService.get("gone")).thenReturn(Optional.of(inactiveEntry));

        assertThatThrownBy(() -> urlService.resolveLongUrl("gone", null, null, "127.0.0.1"))
            .isInstanceOf(UrlNotFoundException.class);
    }

    @Test
    void throwsExpiredForPastExpiryCachedLink() {
        UrlCacheEntry expiredEntry = UrlCacheEntry.builder()
            .id(1L).longUrl("https://example.com").active(true)
            .expiresAt(Instant.now().minusSeconds(60)).build();
        when(urlCacheService.get("old")).thenReturn(Optional.of(expiredEntry));

        assertThatThrownBy(() -> urlService.resolveLongUrl("old", null, null, "127.0.0.1"))
            .isInstanceOf(UrlExpiredException.class);
    }

    @Test
    void deleteRejectsNonOwner() {
        Url url = Url.builder().id(1L).shortCode("mine").userId(5L).active(true).build();
        when(urlRepository.findByShortCode("mine")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.deleteShortUrl("mine", 99L))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteRejectsOwnerlessLink() {
        Url url = Url.builder().id(1L).shortCode("anon").userId(null).active(true).build();
        when(urlRepository.findByShortCode("anon")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.deleteShortUrl("anon", 1L))
            .isInstanceOf(AccessDeniedException.class);
    }
}
