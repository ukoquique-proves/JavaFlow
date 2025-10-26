package com.javaflow.domain.exception;

/**
 * Exception thrown when a bot configuration is not found.
 * 
 * @since 1.0.0
 */
public class BotNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new BotNotFoundException with the bot ID.
     *
     * @param botId The ID of the bot that was not found
     */
    public BotNotFoundException(Long botId) {
        super(String.format("Bot with id %d not found", botId));
    }
    
    /**
     * Constructs a new BotNotFoundException with a custom message.
     *
     * @param message The detail message
     */
    public BotNotFoundException(String message) {
        super(message);
    }
}
