package com.linksnip.urlshortener.controller;

import com.linksnip.urlshortener.service.UrlService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final UrlService urlService;
    private final MeterRegistry meterRegistry;

    public RedirectController(UrlService urlService, MeterRegistry meterRegistry) {
        this.urlService = urlService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String referrer = request.getHeader(HttpHeaders.REFERER);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String clientIp = clientIp(request);

        String longUrl = Timer.builder("urlshortener.redirect.latency")
            .register(meterRegistry)
            .record(() -> urlService.resolveLongUrl(shortCode, referrer, userAgent, clientIp));

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, longUrl)
            .build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
