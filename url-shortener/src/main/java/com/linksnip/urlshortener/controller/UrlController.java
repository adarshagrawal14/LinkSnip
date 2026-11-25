package com.linksnip.urlshortener.controller;

import com.linksnip.urlshortener.dto.request.CreateShortUrlRequest;
import com.linksnip.urlshortener.dto.response.ShortUrlResponse;
import com.linksnip.urlshortener.dto.response.UrlDetailsResponse;
import com.linksnip.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(
        @Valid @RequestBody CreateShortUrlRequest request,
        Authentication authentication
    ) {
        ShortUrlResponse response = urlService.createShortUrl(request, resolveUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlDetailsResponse> getUrlDetails(
        @PathVariable String shortCode,
        Authentication authentication
    ) {
        return ResponseEntity.ok(urlService.getDetails(shortCode, resolveUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<Page<UrlDetailsResponse>> listUrls(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(urlService.listUrls(resolveUserId(authentication), pageable));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode, Authentication authentication) {
        urlService.deleteShortUrl(shortCode, resolveUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
