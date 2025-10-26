package com.javaflow.domain.events;

/**
 * An event that is published when a bot receives an inbound message.
 *
 * @param botId       The ID of the bot configuration.
 * @param chatId      The chat ID where the message was received.
 * @param userId      The user ID who sent the message.
 * @param messageText The content of the message.
 * @param externalId  The external message ID from the bot platform.
 */
public record BotMessageReceivedEvent(
        Long botId,
        String chatId,
        String userId,
        String messageText,
        String externalId
) {
}
