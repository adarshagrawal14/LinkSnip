package com.linksnip.urlshortener.repository;

import com.linksnip.urlshortener.entity.ClickEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    @Query(
        value = "SELECT CAST(clicked_at AS date) AS day, COUNT(*) AS count "
            + "FROM click_events WHERE url_id = :urlId AND clicked_at >= :since "
            + "GROUP BY day ORDER BY day",
        nativeQuery = true
    )
    List<DailyClickCount> countByDaySince(@Param("urlId") Long urlId, @Param("since") Instant since);

    @Query(
        value = "SELECT COALESCE(referrer, 'direct') AS referrer, COUNT(*) AS count "
            + "FROM click_events WHERE url_id = :urlId "
            + "GROUP BY COALESCE(referrer, 'direct') ORDER BY count DESC LIMIT 5",
        nativeQuery = true
    )
    List<ReferrerCount> topReferrers(@Param("urlId") Long urlId);

    interface DailyClickCount {
        LocalDate getDay();

        Long getCount();
    }

    interface ReferrerCount {
        String getReferrer();

        Long getCount();
    }
}
