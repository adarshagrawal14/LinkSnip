package com.linksnip.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.linksnip.urlshortener.repository.ClickEventRepository;
import com.linksnip.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClickTrackingServiceTest {

    @Mock
    private ClickEventRepository clickEventRepository;
    @Mock
    private UrlRepository urlRepository;

    @Test
    void swallowsRepositoryFailuresInsteadOfPropagating() {
        ClickTrackingService service = new ClickTrackingService(clickEventRepository, urlRepository, "test-pepper");
        when(clickEventRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertThatCode(() -> service.recordClickAsync(1L, "https://ref.example", "agent", "1.2.3.4"))
            .doesNotThrowAnyException();
    }
}
