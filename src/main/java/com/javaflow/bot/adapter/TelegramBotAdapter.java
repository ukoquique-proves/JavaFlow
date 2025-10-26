package com.javaflow.bot.adapter;

import com.javaflow.bot.port.BotPort;
import com.javaflow.domain.events.BotMessageReceivedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

/**
 * Adapter for the Telegram Bot platform.
 * Implements the BotPort for outbound messages and listens for inbound messages.
 */
@Component("telegramBotAdapter")
@Slf4j
public class TelegramBotAdapter extends TelegramLongPollingBot implements BotPort {

    private final ApplicationEventPublisher eventPublisher;
    
    @Value("${javaflow.bot.telegram.token:}")
    private String botToken;
    
    @Value("${javaflow.bot.telegram.username:}")
    private String botUsername;

    public TelegramBotAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Procesar actualizaciones recibidas de Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String userId = update.getMessage().getFrom().getId().toString();
            String messageText = update.getMessage().getText();
            Integer messageId = update.getMessage().getMessageId();

            log.info("Received Telegram message from chat {}: {}", chatId, messageText);

            try {
                // Publish an event for the inbound message
                BotMessageReceivedEvent event = new BotMessageReceivedEvent(
                    1L, // TODO: Get this from the bot configuration
                    chatId,
                    userId,
                    messageText,
                    messageId.toString()
                );
                eventPublisher.publishEvent(event);
                log.debug("Published BotMessageReceivedEvent for chat {}", chatId);
            } catch (Exception e) {
                log.error("Error processing inbound Telegram message", e);
                sendMessage(chatId, "An error occurred while processing your message. Please try again later.");
            }
        }
    }



    /**
     * Sends a simple text message to a specific chat via Telegram.
     */
    @Override
    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
            log.info("Message sent to Telegram chat {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Error sending message to Telegram chat {}", chatId, e);
            // Optionally, rethrow as a custom exception
        }
    }

    /**
     * Sends a message with interactive buttons.
     */
    @Override
    public void sendMessageWithButtons(String chatId, String text, Map<String, String> buttons) {
        // TODO: Implement sending with InlineKeyboardMarkup for Telegram
        sendMessage(chatId, text);
    }

    @Override
    public String getBotType() {
        return "telegram";
    }

    @Override
    public void onClosing() {
        super.onClosing();
    }
}
