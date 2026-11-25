package com.linksnip.urlshortener.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient(
        @Value("${spring.data.redis.host:localhost}") String host,
        @Value("${spring.data.redis.port:6379}") int port,
        @Value("${spring.data.redis.password:}") String password,
        @Value("${spring.data.redis.ssl.enabled:false}") boolean sslEnabled
    ) {
        RedisURI.Builder builder = RedisURI.Builder.redis(host, port);
        if (password != null && !password.isBlank()) {
            builder.withPassword(password.toCharArray());
        }
        if (sslEnabled) {
            builder.withSsl(true);
        }
        return RedisClient.create(builder.build());
    }

    @Bean
    public ProxyManager<byte[]> bucketProxyManager(RedisClient rateLimitRedisClient) {
        return LettuceBasedProxyManager.builderFor(rateLimitRedisClient).build();
    }
}
