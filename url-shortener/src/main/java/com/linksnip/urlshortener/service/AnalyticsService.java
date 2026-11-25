package com.linksnip.urlshortener.service;

import com.linksnip.urlshortener.dto.response.AnalyticsResponse;
import com.linksnip.urlshortener.entity.Url;
import com.linksnip.urlshortener.exception.UrlNotFoundException;
import com.linksnip.urlshortener.repository.ClickEventRepository;
import com.linksnip.urlshortener.repository.UrlRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(UrlRepository urlRepository, ClickEventRepository clickEventRepository) {
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String shortCode, Long requestingUserId) {
        Url url = urlRepository.findByShortCode(shortCode)
            .filter(Url::isActive)
            .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (url.getUserId() != null && !url.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You do not have access to this short URL's analytics");
        }

        long totalClicks = clickEventRepository.countByUrlId(url.getId());

        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        var dailyCounts = clickEventRepository.countByDaySince(url.getId(), since).stream()
            .map(row -> new AnalyticsResponse.DailyCount(row.getDay(), row.getCount()))
            .toList();

        var topReferrers = clickEventRepository.topReferrers(url.getId()).stream()
            .map(row -> new AnalyticsResponse.ReferrerCount(row.getReferrer(), row.getCount()))
            .toList();

        return new AnalyticsResponse(shortCode, totalClicks, dailyCounts, topReferrers);
    }
}
