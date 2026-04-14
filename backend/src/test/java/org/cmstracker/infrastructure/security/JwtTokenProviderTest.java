package org.cmstracker.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "test-secret-key-for-unit-testing-only-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    @DisplayName("generateToken: produces a non-blank token")
    void generateToken_valid() {
        String token = tokenProvider.generateToken("testuser");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("getUsernameFromToken: extracts correct username")
    void getUsernameFromToken_returnsCorrectSubject() {
        String token = tokenProvider.generateToken("alice");
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("validateToken: returns true for valid token")
    void validateToken_valid_returnsTrue() {
        String token = tokenProvider.generateToken("bob");
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken: returns false for tampered token")
    void validateToken_tampered_returnsFalse() {
        String token = tokenProvider.generateToken("charlie") + "tampered";
        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken: returns false for empty string")
    void validateToken_empty_returnsFalse() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }
}
