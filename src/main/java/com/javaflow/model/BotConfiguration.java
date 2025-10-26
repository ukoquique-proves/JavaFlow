package com.javaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad de Configuración de Bot (Telegram/WhatsApp)
 */
@Entity
@Table(name = "bot_configurations", indexes = {
    @Index(name = "idx_bot_type", columnList = "type"),
    @Index(name = "idx_bot_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BotType type;

    @Column(length = 500)
    private String token; // Encriptado

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private BotStatus status = BotStatus.INACTIVE;

    @Column(columnDefinition = "TEXT")
    private String config; // Configuración adicional en JSON

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BotType {
        TELEGRAM,
        WHATSAPP
    }

    public enum BotStatus {
        ACTIVE,
        INACTIVE,
        ERROR
    }
}
