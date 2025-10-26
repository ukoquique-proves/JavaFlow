package com.javaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad de Mensaje (Telegram/WhatsApp)
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_bot_id", columnList = "bot_id"),
    @Index(name = "idx_message_chat_id", columnList = "chat_id"),
    @Index(name = "idx_message_created_at", columnList = "created_at"),
    @Index(name = "idx_message_workflow_execution_id", columnList = "workflow_execution_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private BotConfiguration bot;

    @Column(name = "external_id")
    private String externalId; // ID del mensaje en la plataforma externa

    @NotBlank
    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageDirection direction;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 50)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String metadata; // Metadata adicional en JSON

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecution workflowExecution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageDirection {
        INBOUND,
        OUTBOUND
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        LOCATION,
        CONTACT
    }
}
