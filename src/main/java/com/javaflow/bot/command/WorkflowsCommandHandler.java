package com.javaflow.bot.command;

import com.javaflow.monitoring.MetricsService;
import com.javaflow.service.BotService;
import com.javaflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Handles the /workflows command for bots.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowsCommandHandler implements BotCommandHandler {
    
    @Lazy
    private final BotService botService;
    private final WorkflowService workflowService;
    private final MetricsService metricsService;
    
    @Override
    public String getCommand() {
        return "/workflows";
    }
    
    @Override
    public void handle(CommandContext context) {
        log.debug("Handling /workflows command for bot {} in chat {}", context.botId(), context.chatId());
        
        String botType = context.bot().getType().name().toLowerCase();
        metricsService.recordBotCommand(botType, "workflows");
        
        StringBuilder response = new StringBuilder("📋 Workflows disponibles:\n\n");
        workflowService.getAllWorkflows().forEach(w ->
            response.append("• ")
                    .append(w.getName())
                    .append(" (")
                    .append(w.getStatus())
                    .append(")\n")
        );
        
        botService.sendMessage(context.botId(), context.chatId(), response.toString());
    }
}
