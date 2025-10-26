package com.javaflow.bot.telegram;

import com.javaflow.bot.adapter.TelegramBotAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Configuración del Bot de Telegram
 */
@Configuration
@ConditionalOnProperty(prefix = "javaflow.bot.telegram", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig {

    private final TelegramBotAdapter telegramBotAdapter;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        log.info("Initializing Telegram Bot API");
        
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBotAdapter);
        
        log.info("Telegram bot registered successfully");
        return botsApi;
    }
}
