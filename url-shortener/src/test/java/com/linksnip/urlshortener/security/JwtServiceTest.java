package com.linksnip.urlshortener.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-unit-tests-min-32-bytes!!";

    private final JwtService jwtService = new JwtService(SECRET, 1000L * 60 * 60);

    @Test
    void issuesAndParsesTokenRoundTrip() {
        String token = jwtService.issueToken(42L, "user@example.com");
        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService alreadyExpired = new JwtService(SECRET, -1000L);
        String token = alreadyExpired.issueToken(1L, "user@example.com");

        assertThatThrownBy(() -> alreadyExpired.parseUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.issueToken(1L, "user@example.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseUserId(tampered)).isInstanceOf(JwtException.class);
    }
}
