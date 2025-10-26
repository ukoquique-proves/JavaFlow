package com.javaflow.application.dto.bot;

import com.javaflow.model.Message;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response DTO for message operations
 */
@Value
@Builder
public class MessageResponse {
    
    Long id;
    Long botId;
    String botName;
    String chatId;
    String userId;
    String content;
    String direction; // INBOUND, OUTBOUND
    String messageType; // TEXT, IMAGE, etc.
    LocalDateTime createdAt;
    String externalId;
    
    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .botId(message.getBot().getId())
                .botName(message.getBot().getName())
                .chatId(message.getChatId())
                .userId(message.getUserId())
                .content(message.getContent())
                .direction(message.getDirection().toString())
                .messageType(message.getMessageType().toString())
                .createdAt(message.getCreatedAt())
                .externalId(message.getExternalId())
                .build();
    }
}