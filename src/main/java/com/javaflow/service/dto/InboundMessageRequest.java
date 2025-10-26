package com.javaflow.service.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Request object for processing inbound bot messages.
 * Reduces parameter list from 5 to 1 parameter.
 * 
 * @since 1.0.0
 */
@Value
@Builder
public class InboundMessageRequest {
    Long botId;
    String chatId;
    String userId;
    String content;
    String externalId;
    
    /**
     * Creates a request for an inbound message.
     */
    public static InboundMessageRequest of(Long botId, String chatId, String userId, 
                                          String content, String externalId) {
        return InboundMessageRequest.builder()
                .botId(botId)
                .chatId(chatId)
                .userId(userId)
                .content(content)
                .externalId(externalId)
                .build();
    }
}
