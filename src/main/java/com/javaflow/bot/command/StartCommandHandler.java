package com.javaflow.bot.command;

import com.javaflow.monitoring.MetricsService;
import com.javaflow.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Handles the /start command for bots.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommandHandler implements BotCommandHandler {
    
    @Lazy
    private final BotService botService;
    private final MetricsService metricsService;
    
    @Override
    public String getCommand() {
        return "/start";
    }
    
    @Override
    public void handle(CommandContext context) {
        log.debug("Handling /start command for bot {} in chat {}", context.botId(), context.chatId());
        
        String botType = context.bot().getType().name().toLowerCase();
        metricsService.recordBotCommand(botType, "start");
        
        String welcomeMessage = """
            ¡Bienvenido a JavaFlow! 🚀
            
            Comandos disponibles:
            /help - Mostrar ayuda
            /status - Ver estado del sistema
            /workflows - Listar workflows disponibles
            """;
        
        botService.sendMessage(context.botId(), context.chatId(), welcomeMessage);
    }
}
