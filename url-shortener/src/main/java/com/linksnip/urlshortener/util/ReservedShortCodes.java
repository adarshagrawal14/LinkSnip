package com.linksnip.urlshortener.util;

import java.util.Set;

public final class ReservedShortCodes {

    public static final Set<String> RESERVED = Set.of(
        "api", "admin", "actuator", "swagger", "swagger-ui", "v1", "health",
        "static", "favicon.ico", "urls", "auth", "login", "register", "www",
        "app", "docs", "metrics", "prometheus", "robots.txt", "assets",
        "public", "null", "undefined"
    );

    private ReservedShortCodes() {
    }

    public static boolean isReserved(String code) {
        return RESERVED.contains(code.toLowerCase());
    }
}
