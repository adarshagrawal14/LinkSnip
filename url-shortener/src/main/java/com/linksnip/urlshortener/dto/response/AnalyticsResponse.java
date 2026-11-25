package com.linksnip.urlshortener.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
    String shortCode,
    long totalClicks,
    List<DailyCount> clicksLast7Days,
    List<ReferrerCount> topReferrers
) {
    public record DailyCount(LocalDate date, long count) {
    }

    public record ReferrerCount(String referrer, long count) {
    }
}
