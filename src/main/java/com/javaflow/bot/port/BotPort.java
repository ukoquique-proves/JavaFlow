package com.javaflow.bot.port;

import java.util.Map;

/**
 * Port for outbound communication with a bot platform.
 * This defines the contract for sending messages, regardless of the platform (Telegram, WhatsApp, etc.).
 */
public interface BotPort {

    /**
     * Sends a simple text message to a specific chat.
     *
     * @param chatId The unique identifier for the target chat.
     * @param text   The message content to send.
     */
    void sendMessage(String chatId, String text);

    /**
     * Sends a message with interactive buttons.
     *
     * @param chatId  The unique identifier for the target chat.
     * @param text    The message content to send.
     * @param buttons A map of button text to callback data.
     */
    void sendMessageWithButtons(String chatId, String text, Map<String, String> buttons);

    /**
     * Gets the unique type of the bot (e.g., "telegram", "whatsapp").
     *
     * @return The bot type identifier.
     */
    String getBotType();

}
