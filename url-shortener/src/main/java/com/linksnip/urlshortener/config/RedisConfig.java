package com.linksnip.urlshortener.config;

import com.linksnip.urlshortener.service.UrlCacheEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, UrlCacheEntry> urlCacheRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UrlCacheEntry> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(UrlCacheEntry.class));
        template.afterPropertiesSet();
        return template;
    }
}
