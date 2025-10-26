package com.javaflow.bot.adapter;

import com.javaflow.bot.port.BotPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Placeholder adapter for WhatsApp.
 * This demonstrates how a new bot platform can be integrated.
 */
@Component("whatsappBotAdapter")
@Slf4j
public class WhatsAppBotAdapter implements BotPort {

    @Override
    public void sendMessage(String chatId, String text) {
        log.warn("WhatsApp adapter is not yet implemented. Message not sent to {}: {}", chatId, text);
        // TODO: Implement actual WhatsApp sending logic here (e.g., via whatsapp-web.js bridge)
    }

    @Override
    public void sendMessageWithButtons(String chatId, String text, Map<String, String> buttons) {
        log.warn("WhatsApp adapter is not yet implemented. Message with buttons not sent.");
        // TODO: Implement button sending logic for WhatsApp
    }

    @Override
    public String getBotType() {
        return "whatsapp";
    }
}
