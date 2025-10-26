package com.javaflow.application.dto.bot;

import com.javaflow.model.BotConfiguration;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response DTO for bot configuration operations
 */
@Value
@Builder
public class BotResponse {
    
    Long id;
    String name;
    String type;
    String status;
    String webhookUrl;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Security: Don't expose the actual token
    boolean hasToken;
    
    public static BotResponse from(BotConfiguration bot) {
        return BotResponse.builder()
                .id(bot.getId())
                .name(bot.getName())
                .type(bot.getType().toString())
                .status(bot.getStatus().toString())
                .webhookUrl(bot.getWebhookUrl())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .hasToken(bot.getToken() != null && !bot.getToken().isEmpty())
                .build();
    }
}