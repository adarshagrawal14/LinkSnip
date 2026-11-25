package com.linksnip.urlshortener.ratelimit;

import com.linksnip.urlshortener.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final BucketResolver bucketResolver;
    private final RateLimitProperties properties;
    private final MeterRegistry meterRegistry;

    public RateLimitFilter(BucketResolver bucketResolver, RateLimitProperties properties, MeterRegistry meterRegistry) {
        this.bucketResolver = bucketResolver;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        String bucketKey;
        String tierName;
        RateLimitProperties.Tier tier;

        if ("POST".equals(method) && "/api/v1/urls".equals(path)) {
            Long userId = currentUserId();
            if (userId != null) {
                bucketKey = "create:user:" + userId;
                tierName = "create-authenticated";
                tier = properties.getAuthenticatedCreate();
            } else {
                bucketKey = "create:ip:" + clientIp(request);
                tierName = "create-anonymous";
                tier = properties.getAnonymousCreate();
            }
        } else if ("POST".equals(method) && "/api/v1/auth/login".equals(path)) {
            bucketKey = "login:ip:" + clientIp(request);
            tierName = "login";
            tier = properties.getLogin();
        } else if ("GET".equals(method) && isRedirectPath(path)) {
            bucketKey = "redirect:ip:" + clientIp(request);
            tierName = "redirect";
            tier = properties.getRedirect();
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        Bandwidth bandwidth = Bandwidth.simple(tier.getCapacity(), tier.getPeriod());
        BucketProxy bucket = bucketResolver.resolve(bucketKey, bandwidth);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        meterRegistry.counter("urlshortener.ratelimit.rejected", "tier", tierName).increment();

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"Too Many Requests\",\"retryAfterSeconds\":" + retryAfterSeconds + "}"
        );
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private boolean isRedirectPath(String path) {
        return path.matches("/[a-zA-Z0-9_-]+");
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
