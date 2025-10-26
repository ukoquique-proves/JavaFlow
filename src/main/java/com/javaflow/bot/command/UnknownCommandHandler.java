package com.javaflow.bot.command;

import com.javaflow.monitoring.MetricsService;
import com.javaflow.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Handles unknown/unrecognized bot commands.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnknownCommandHandler implements BotCommandHandler {
    
    @Lazy
    private final BotService botService;
    private final MetricsService metricsService;
    
    @Override
    public String getCommand() {
        return "unknown";
    }
    
    @Override
    public void handle(CommandContext context) {
        log.debug("Handling unknown command '{}' for bot {} in chat {}", 
                  context.messageText(), context.botId(), context.chatId());
        
        String botType = context.bot().getType().name().toLowerCase();
        metricsService.recordBotCommand(botType, "unknown");
        
        String message = "Comando desconocido. Usa /help para ver los comandos disponibles.";
        botService.sendMessage(context.botId(), context.chatId(), message);
    }
}
