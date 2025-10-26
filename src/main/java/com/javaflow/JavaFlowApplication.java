package com.javaflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JavaFlow - Sistema de Workflow tipo n8n
 * 
 * Sistema completo de gestión de workflows con:
 * - Spring Boot 3.x
 * - Vaadin 24 (UI)
 * - Flowable BPMN Engine
 * - PostgreSQL
 * - Telegram/WhatsApp Bots
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@Slf4j
public class JavaFlowApplication {

    public static void main(String[] args) {
        log.info("🚀 Iniciando JavaFlow...");
        SpringApplication.run(JavaFlowApplication.class, args);
        log.info("✅ JavaFlow iniciada exitosamente!");
        log.info("🌐 Acceder a: http://localhost:8080");
        log.info("💚 Health: http://localhost:8080/actuator/health");
    }

}
