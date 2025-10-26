package com.javaflow.application.dto.bot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * Request DTO for creating a new bot configuration
 */
@Value
@Builder
public class CreateBotRequest {
    
    @NotBlank(message = "Bot name is required")
    @Size(min = 3, max = 100, message = "Bot name must be between 3 and 100 characters")
    String name;
    
    @NotNull(message = "Bot type is required")
    String type; // TELEGRAM, WHATSAPP
    
    @NotBlank(message = "Bot token is required")
    String token;
    
    String webhookUrl;
    
    String config; // JSON configuration
    
    public static CreateBotRequest of(String name, String type, String token) {
        return CreateBotRequest.builder()
                .name(name)
                .type(type)
                .token(token)
                .build();
    }
}