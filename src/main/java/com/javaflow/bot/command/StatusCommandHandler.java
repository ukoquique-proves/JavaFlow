package com.javaflow.bot.command;

import com.javaflow.model.Workflow;
import com.javaflow.monitoring.MetricsService;
import com.javaflow.service.BotService;
import com.javaflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Handles the /status command for bots.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatusCommandHandler implements BotCommandHandler {
    
    @Lazy
    private final BotService botService;
    private final WorkflowService workflowService;
    private final MetricsService metricsService;
    
    @Override
    public String getCommand() {
        return "/status";
    }
    
    @Override
    public void handle(CommandContext context) {
        log.debug("Handling /status command for bot {} in chat {}", context.botId(), context.chatId());
        
        String botType = context.bot().getType().name().toLowerCase();
        metricsService.recordBotCommand(botType, "status");
        
        long activeWorkflows = workflowService.getAllWorkflows().stream()
                .filter(w -> w.getStatus() == Workflow.WorkflowStatus.ACTIVE)
                .count();
        
        String statusMessage = String.format(
            "✅ Sistema operativo\nWorkflows activos: %d",
            activeWorkflows
        );
        
        botService.sendMessage(context.botId(), context.chatId(), statusMessage);
    }
}
