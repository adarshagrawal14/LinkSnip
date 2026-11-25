package com.linksnip.urlshortener.repository;

import com.linksnip.urlshortener.entity.Url;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Page<Url> findByUserIdAndActiveTrue(Long userId, Pageable pageable);

    @Query(value = "SELECT nextval('urls_id_seq')", nativeQuery = true)
    Long nextId();

    @Modifying
    @Query(value = "UPDATE urls SET click_count = click_count + 1 WHERE id = :id", nativeQuery = true)
    void incrementClickCount(@Param("id") Long id);
}
