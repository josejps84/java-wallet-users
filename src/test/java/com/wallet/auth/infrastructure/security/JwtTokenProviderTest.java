package com.wallet.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Test
    void tokenCarriesTheClaimsTheOtherServicesRead() {
        String token = new JwtTokenProvider(SECRET, 60_000).generateToken("jane@example.com", "ADMIN", 42L);

        Claims claims = parse(token);
        assertThat(claims.getSubject()).isEqualTo("jane@example.com");
        assertThat(claims.get("userType", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.getExpiration().toInstant())
                .isBetween(Instant.now().plus(Duration.ofSeconds(50)), Instant.now().plus(Duration.ofSeconds(61)));
    }

    @Test
    void tokenExpiresAfterTheConfiguredTime() {
        String token = new JwtTokenProvider(SECRET, -1_000).generateToken("jane@example.com", "CUSTOMER", 1L);

        assertThatThrownBy(() -> parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void secretsShorterThan256BitsAreRejected() {
        assertThatThrownBy(() -> new JwtTokenProvider("too-short", 60_000)).isInstanceOf(WeakKeyException.class);
    }

    private static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
