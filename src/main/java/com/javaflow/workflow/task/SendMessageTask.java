package com.javaflow.workflow.task;

import com.javaflow.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Tarea personalizada de Flowable para enviar mensajes
 * 
 * Variables esperadas:
 * - chatId: ID del chat destino
 * - message: Texto del mensaje
 * - botType: TELEGRAM o WHATSAPP
 */
@Component("sendMessageTask")
@RequiredArgsConstructor
@Slf4j
public class SendMessageTask implements JavaDelegate {

    private final BotService botService;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing SendMessageTask");
        
        String chatId = (String) execution.getVariable("chatId");
        String message = (String) execution.getVariable("message");
        Long botId = (Long) execution.getVariable("botId");

        if (chatId == null || message == null || botId == null) {
            throw new RuntimeException("botId, chatId, and message are required variables");
        }

        try {
            botService.sendMessage(botId, chatId, message);
            log.info("Message sent via BotService to chat: {}", chatId);
            
            execution.setVariable("messageSent", true);
            execution.setVariable("messageTimestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            execution.setVariable("messageSent", false);
            execution.setVariable("error", e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }
}
