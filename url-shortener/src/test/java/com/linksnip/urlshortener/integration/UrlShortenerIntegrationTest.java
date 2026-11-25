package com.linksnip.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linksnip.urlshortener.service.UrlCacheEntry;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class UrlShortenerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("urlCacheRedisTemplate")
    private RedisTemplate<String, UrlCacheEntry> cacheRedisTemplate;

    private String registerAndLogin(String email) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    @Test
    void fullCreateAndRedirectHappyPath() throws Exception {
        String token = registerAndLogin("happy-path@example.com");

        mockMvc.perform(post("/api/v1/urls")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/happy\",\"customAlias\":\"happypath\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").value("happypath"));

        mockMvc.perform(get("/happypath"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "https://example.com/happy"));
    }

    @Test
    void duplicateCustomAliasReturns409() throws Exception {
        String token = registerAndLogin("dup-alias@example.com");
        String body = "{\"longUrl\":\"https://example.com/a\",\"customAlias\":\"duplicatetest\"}";

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }

    @Test
    void reservedAliasReturns422() throws Exception {
        String token = registerAndLogin("reserved-alias@example.com");

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/a\",\"customAlias\":\"admin\"}"))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void expiredLinkReturns410() throws Exception {
        String token = registerAndLogin("expiry-test@example.com");
        String expiresAt = Instant.now().plusSeconds(2).toString();

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/expiring\",\"customAlias\":\"expiringlink\","
                    + "\"expiresAt\":\"" + expiresAt + "\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/expiringlink")).andExpect(status().isFound());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            mockMvc.perform(get("/expiringlink")).andExpect(status().isGone())
        );
    }

    @Test
    void cacheIsPopulatedOnFirstRedirectAndHitOnSecond() throws Exception {
        String token = registerAndLogin("cache-test@example.com");

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/cached\",\"customAlias\":\"cachedlink\"}"))
            .andExpect(status().isCreated());

        assertThat(cacheRedisTemplate.hasKey("urlshortener:url:cachedlink")).isFalse();

        mockMvc.perform(get("/cachedlink")).andExpect(status().isFound());

        assertThat(cacheRedisTemplate.hasKey("urlshortener:url:cachedlink")).isTrue();
        UrlCacheEntry cached = cacheRedisTemplate.opsForValue().get("urlshortener:url:cachedlink");
        assertThat(cached.getLongUrl()).isEqualTo("https://example.com/cached");
    }

    @Test
    void deletingLinkEvictsCacheAndSubsequentRedirect404s() throws Exception {
        String token = registerAndLogin("delete-test@example.com");

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/deleteme\",\"customAlias\":\"deletelink\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/deletelink")).andExpect(status().isFound());
        assertThat(cacheRedisTemplate.hasKey("urlshortener:url:deletelink")).isTrue();

        mockMvc.perform(delete("/api/v1/urls/deletelink").header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        assertThat(cacheRedisTemplate.hasKey("urlshortener:url:deletelink")).isFalse();
        mockMvc.perform(get("/deletelink")).andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedAndWrongOwnerDeleteAreRejected() throws Exception {
        String ownerToken = registerAndLogin("owner@example.com");
        String otherToken = registerAndLogin("other-user@example.com");

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/owned\",\"customAlias\":\"ownedlink\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/urls/ownedlink"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/urls/ownedlink").header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/urls/ownedlink").header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isNoContent());
    }

    @Test
    void rateLimitExceededOnAnonymousCreateReturns429() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"longUrl\":\"https://example.com/rl" + i + "\"}"))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/rl-over\"}"))
            .andExpect(status().is(429));
    }

    @Test
    void analyticsAggregatesClicksAndReferrers() throws Exception {
        String token = registerAndLogin("analytics-flow@example.com");

        mockMvc.perform(post("/api/v1/urls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com/tracked\",\"customAlias\":\"trackedlink\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/trackedlink").header("Referer", "https://twitter.com")).andExpect(status().isFound());
        mockMvc.perform(get("/trackedlink").header("Referer", "https://twitter.com")).andExpect(status().isFound());
        mockMvc.perform(get("/trackedlink")).andExpect(status().isFound());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            mockMvc.perform(get("/api/v1/urls/trackedlink/analytics").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(3))
        );
    }
}
