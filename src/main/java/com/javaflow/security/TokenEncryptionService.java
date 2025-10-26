package com.javaflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive tokens using AES-256-GCM.
 * 
 * <p>This service provides secure encryption for bot tokens and other sensitive
 * credentials stored in the database. It uses AES-256 in GCM mode which provides
 * both confidentiality and authenticity.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * @Service
 * public class BotService {
 *     private final TokenEncryptionService encryptionService;
 *     
 *     public void saveBot(String token) {
 *         String encrypted = encryptionService.encrypt(token);
 *         // Save encrypted token to database
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
@Service
@Slf4j
public class TokenEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * Constructs the encryption service with the configured encryption key.
     *
     * @param encryptionKey Base64-encoded 256-bit encryption key from configuration
     * @throws IllegalArgumentException if the encryption key is invalid
     */
    public TokenEncryptionService(
            @Value("${javaflow.security.encryption.key:}") String encryptionKey) {
        
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            log.warn("No encryption key configured. Using default key (NOT SECURE FOR PRODUCTION)");
            // Default 32-byte key encoded in Base64: "12345678901234567890123456789012"
            encryptionKey = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException(
                    "Encryption key must be 256 bits (32 bytes). Got: " + keyBytes.length + " bytes"
                );
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            this.secureRandom = new SecureRandom();
            
            log.info("TokenEncryptionService initialized successfully");
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize encryption service", e);
        }
    }

    /**
     * Encrypts a plaintext token using AES-256-GCM.
     *
     * @param plaintext The plaintext token to encrypt
     * @return Base64-encoded encrypted token with IV prepended
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Plaintext cannot be null or empty");
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);
            
            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // Encode to Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            log.error("Failed to encrypt token", e);
            throw new EncryptionException("Failed to encrypt token", e);
        }
    }

    /**
     * Decrypts an encrypted token using AES-256-GCM.
     *
     * @param encryptedToken Base64-encoded encrypted token with IV prepended
     * @return Decrypted plaintext token
     * @throws EncryptionException if decryption fails
     */
    public String decrypt(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            throw new IllegalArgumentException("Encrypted token cannot be null or empty");
        }
        
        try {
            // Decode from Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedToken);
            
            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to decrypt token", e);
            throw new EncryptionException("Failed to decrypt token", e);
        }
    }

    /**
     * Exception thrown when encryption or decryption operations fail.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
