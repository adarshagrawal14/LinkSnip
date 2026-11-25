package com.linksnip.urlshortener.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class BucketResolver {

    private static final String KEY_PREFIX = "urlshortener:ratelimit:";

    private final ProxyManager<byte[]> proxyManager;

    public BucketResolver(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public BucketProxy resolve(String key, Bandwidth bandwidth) {
        byte[] keyBytes = (KEY_PREFIX + key).getBytes(StandardCharsets.UTF_8);
        BucketConfiguration configuration = BucketConfiguration.builder().addLimit(bandwidth).build();
        return proxyManager.getProxy(keyBytes, () -> configuration);
    }
}
