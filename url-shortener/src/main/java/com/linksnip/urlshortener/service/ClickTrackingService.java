package com.linksnip.urlshortener.service;

import com.linksnip.urlshortener.entity.ClickEvent;
import com.linksnip.urlshortener.repository.ClickEventRepository;
import com.linksnip.urlshortener.repository.UrlRepository;
import com.linksnip.urlshortener.util.IpHashUtil;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingService.class);

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;
    private final String ipHashPepper;

    public ClickTrackingService(
        ClickEventRepository clickEventRepository,
        UrlRepository urlRepository,
        @Value("${app.ip-hash-pepper}") String ipHashPepper
    ) {
        this.clickEventRepository = clickEventRepository;
        this.urlRepository = urlRepository;
        this.ipHashPepper = ipHashPepper;
    }

    @Async("clickTrackingExecutor")
    @Transactional
    public void recordClickAsync(Long urlId, String referrer, String userAgent, String ipAddress) {
        try {
            ClickEvent event = ClickEvent.builder()
                .urlId(urlId)
                .clickedAt(Instant.now())
                .referrer(referrer)
                .userAgent(userAgent)
                .ipHash(IpHashUtil.hash(ipAddress, ipHashPepper))
                .build();

            clickEventRepository.save(event);
            urlRepository.incrementClickCount(urlId);
        } catch (Exception ex) {
            log.warn("Failed to record click for urlId={}", urlId, ex);
        }
    }
}
