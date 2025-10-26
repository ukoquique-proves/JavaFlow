package com.javaflow.bot.command;

import com.javaflow.model.BotConfiguration;

/**
 * Interface for handling bot commands.
 * 
 * <p>Implementations of this interface handle specific bot commands (e.g., /start, /help).
 * This follows the Command pattern to make the bot extensible and maintainable.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * @Component
 * public class StartCommandHandler implements BotCommandHandler {
 *     @Override
 *     public String getCommand() {
 *         return "/start";
 *     }
 *     
 *     @Override
 *     public void handle(CommandContext context) {
 *         // Handle /start command
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
public interface BotCommandHandler {
    
    /**
     * Returns the command this handler processes (e.g., "/start", "/help").
     *
     * @return The command string
     */
    String getCommand();
    
    /**
     * Handles the bot command.
     *
     * @param context The command execution context
     */
    void handle(CommandContext context);
    
    /**
     * Context object containing all information needed to handle a command.
     */
    record CommandContext(
        Long botId,
        String chatId,
        String messageText,
        BotConfiguration bot
    ) {}
}
