package com.linksnip.urlshortener.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private Tier anonymousCreate = new Tier(5, Duration.ofMinutes(1));
    private Tier authenticatedCreate = new Tier(30, Duration.ofMinutes(1));
    private Tier redirect = new Tier(100, Duration.ofMinutes(1));
    private Tier login = new Tier(5, Duration.ofMinutes(15));

    @Getter
    @Setter
    public static class Tier {
        private long capacity;
        private Duration period;

        public Tier() {
        }

        public Tier(long capacity, Duration period) {
            this.capacity = capacity;
            this.period = period;
        }
    }
}
