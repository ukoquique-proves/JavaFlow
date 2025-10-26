package com.javaflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenEncryptionServiceTest {

    // 32-byte key encoded in Base64: "12345678901234567890123456789012"
    private static final String TEST_ENCRYPTION_KEY = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    @Test
    void shouldEncryptAndDecryptTokenSuccessfully() {
        // Given
        TokenEncryptionService service = new TokenEncryptionService(TEST_ENCRYPTION_KEY);

        // When
        String originalToken = "test-bot-token-12345";
        String encrypted = service.encrypt(originalToken);
        String decrypted = service.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(originalToken);
        assertThat(encrypted).isNotEqualTo(originalToken);
        assertThat(encrypted.length()).isGreaterThan(50); // Should be Base64 encoded with IV
    }

    @Test
    void shouldThrowExceptionWhenEncryptingNull() {
        // Given
        TokenEncryptionService service = new TokenEncryptionService(TEST_ENCRYPTION_KEY);

        // When/Then
        assertThatThrownBy(() -> service.encrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenDecryptingInvalidToken() {
        // Given
        TokenEncryptionService service = new TokenEncryptionService(TEST_ENCRYPTION_KEY);

        // When/Then
        assertThatThrownBy(() -> service.decrypt("invalid-base64-token"))
                .isInstanceOf(TokenEncryptionService.EncryptionException.class);
    }
}
