package com.javaflow.service;

import com.javaflow.bot.port.BotPort;
import com.javaflow.domain.events.BotMessageReceivedEvent;
import com.javaflow.model.BotConfiguration;
import com.javaflow.model.Message;
import com.javaflow.repository.BotConfigurationRepository;
import com.javaflow.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de Bots
 */
@Service
@Slf4j
public class BotService {

    private final BotConfigurationRepository botRepository;
    private final MessageRepository messageRepository;
    private final Map<String, BotPort> botAdapters;
    private final WorkflowService workflowService;
    private final com.javaflow.monitoring.MetricsService metricsService;
    private final com.javaflow.security.TokenEncryptionService encryptionService;
    private final ApplicationContext applicationContext;
    private final com.javaflow.bot.command.UnknownCommandHandler unknownCommandHandler;

    @Lazy
    public BotService(
        BotConfigurationRepository botRepository, 
        MessageRepository messageRepository, 
        Map<String, BotPort> botAdapters, 
        WorkflowService workflowService, 
        com.javaflow.monitoring.MetricsService metricsService, 
        com.javaflow.security.TokenEncryptionService encryptionService, 
        ApplicationContext applicationContext, 
        com.javaflow.bot.command.UnknownCommandHandler unknownCommandHandler
    ) {
        this.botRepository = botRepository;
        this.messageRepository = messageRepository;
        this.botAdapters = botAdapters;
        this.workflowService = workflowService;
        this.metricsService = metricsService;
        this.encryptionService = encryptionService;
        this.applicationContext = applicationContext;
        this.unknownCommandHandler = unknownCommandHandler;
    }

    /**
     * Crear configuración de bot
     */
    @Transactional
    public BotConfiguration createBot(String name, BotConfiguration.BotType type, String token) {
        log.info("Creating bot: {} of type {}", name, type);
        
        BotConfiguration bot = BotConfiguration.builder()
                .name(name)
                .type(type)
                .token(encryptToken(token))
                .status(BotConfiguration.BotStatus.INACTIVE)
                .build();
        
        return botRepository.save(bot);
    }

    /**
     * Obtener bot por ID
     */
    public BotConfiguration getBot(Long id) {
        return botRepository.findById(id)
                .orElseThrow(() -> new com.javaflow.domain.exception.BotNotFoundException(id));
    }

    /**
     * Listar todos los bots
     */
    public List<BotConfiguration> getAllBots() {
        return botRepository.findAll();
    }

    /**
     * Listar bots activos
     */
    public List<BotConfiguration> getActiveBots() {
        return botRepository.findByStatus(BotConfiguration.BotStatus.ACTIVE);
    }

    /**
     * Listar bots por tipo
     */
    public List<BotConfiguration> getBotsByType(BotConfiguration.BotType type) {
        return botRepository.findByType(type);
    }

    /**
     * Activar bot
     */
    @Transactional
    public BotConfiguration activateBot(Long id) {
        log.info("Activating bot: {}", id);
        
        BotConfiguration bot = getBot(id);
        bot.setStatus(BotConfiguration.BotStatus.ACTIVE);
        return botRepository.save(bot);
    }

    /**
     * Desactivar bot
     */
    @Transactional
    public BotConfiguration deactivateBot(Long id) {
        log.info("Deactivating bot: {}", id);
        
        BotConfiguration bot = getBot(id);
        bot.setStatus(BotConfiguration.BotStatus.INACTIVE);
        return botRepository.save(bot);
    }

    /**
     * Processes an inbound message, saves it, and handles any associated commands or workflows.
     * 
     * @param request The inbound message request containing all message details
     */
    public void processInboundMessage(com.javaflow.service.dto.InboundMessageRequest request) {
        // 1. Save the inbound message
        saveInboundMessage(request.getBotId(), request.getChatId(), request.getUserId(), 
                          request.getContent(), request.getExternalId());

        // 2. Process commands or trigger workflows
        if (request.getContent().startsWith("/")) {
            processCommand(request.getBotId(), request.getChatId(), request.getContent());
        } else {
            handleNormalMessage(request.getBotId(), request.getChatId(), request.getContent());
        }
    }
    
    /**
     * @deprecated Use {@link #processInboundMessage(InboundMessageRequest)} instead.
     * This method will be removed in version 2.0.
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public void processInboundMessage(Long botId, String chatId, String userId, String content, String externalId) {
        processInboundMessage(com.javaflow.service.dto.InboundMessageRequest.of(
            botId, chatId, userId, content, externalId
        ));
    }

    /**
     * Guardar mensaje recibido
     */
    @Transactional
    public Message saveInboundMessage(Long botId, String chatId, String userId, 
                                     String content, String externalId) {
        log.debug("Saving inbound message from chat: {}", chatId);
        return saveMessage(botId, chatId, userId, content, externalId, Message.MessageDirection.INBOUND);
    }

    /**
     * Sends a message using the appropriate bot adapter and saves it to the database.
     *
     * @param botId   The ID of the bot configuration to use.
     * @param chatId  The ID of the chat to send the message to.
     * @param content The message text.
     */
    public void sendMessage(Long botId, String chatId, String content) {
        BotConfiguration botConfig = getBot(botId);
        // Construct adapter name, e.g., "telegram" -> "telegramBotAdapter"
        String adapterName = botConfig.getType().name().toLowerCase() + "BotAdapter";

        BotPort botAdapter = botAdapters.get(adapterName);
        if (botAdapter == null) {
            log.error("No bot adapter found for name: {}", adapterName);
            throw new IllegalStateException("Unsupported bot type: " + botConfig.getType());
        }

        // 1. Send the message via the adapter
        botAdapter.sendMessage(chatId, content);

        // 2. Record metrics
        metricsService.recordBotMessageSent(botConfig.getType().name().toLowerCase());

        // 3. Save the outbound message to the database
        saveOutboundMessage(botId, chatId, content, null);
    }

    /**
     * Guardar mensaje enviado
     */
    @Transactional
    public Message saveOutboundMessage(Long botId, String chatId, String content, String externalId) {
        log.debug("Saving outbound message to chat: {}", chatId);
        return saveMessage(botId, chatId, null, content, externalId, Message.MessageDirection.OUTBOUND);
    }
    
    /**
     * Helper method to save messages (eliminates duplication between inbound/outbound)
     */
    private Message saveMessage(Long botId, String chatId, String userId, 
                               String content, String externalId, Message.MessageDirection direction) {
        BotConfiguration bot = getBot(botId);
        
        Message message = Message.builder()
                .bot(bot)
                .chatId(chatId)
                .userId(userId)
                .content(content)
                .externalId(externalId)
                .direction(direction)
                .messageType(Message.MessageType.TEXT)
                .build();
        
        return messageRepository.save(message);
    }

    /**
     * Obtener mensajes de un chat
     */
    public List<Message> getMessagesByChatId(String chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId);
    }

    /**
     * Obtener mensajes de un bot
     */
    public List<Message> getMessagesByBot(Long botId) {
        return messageRepository.findByBotId(botId);
    }

    /**
     * Encrypts a bot token using AES-256-GCM encryption.
     * 
     * @param token The plaintext token to encrypt
     * @return Encrypted token (Base64-encoded)
     */
    private String encryptToken(String token) {
        return encryptionService.encrypt(token);
    }

    /**
     * Decrypts a bot token.
     * 
     * @param encryptedToken The encrypted token to decrypt
     * @return Decrypted plaintext token
     */
    public String decryptToken(String encryptedToken) {
        return encryptionService.decrypt(encryptedToken);
    }

    /**
     * Eliminar bot
     */
    @Transactional
    public void deleteBot(Long id) {
        log.info("Deleting bot: {}", id);
        botRepository.deleteById(id);
    }

    // ========== PRIVATE HELPER METHODS FOR MESSAGE PROCESSING ==========

    /**
     * Processes a bot command using the command handler pattern.
     * 
     * @param botId The bot ID
     * @param chatId The chat ID
     * @param messageText The command text
     */
    private void processCommand(Long botId, String chatId, String messageText) {
        BotConfiguration bot = getBot(botId);
        
        // Extract command (first word)
        String command = messageText.split(" ")[0];
        
        // Find and execute handler
        com.javaflow.bot.command.BotCommandHandler handler = getCommandHandler(command);
        if (handler == null) {
            handler = unknownCommandHandler;
        }
        
        com.javaflow.bot.command.BotCommandHandler.CommandContext context = 
            new com.javaflow.bot.command.BotCommandHandler.CommandContext(botId, chatId, messageText, bot);
        
        handler.handle(context);
    }

    private void handleNormalMessage(Long botId, String chatId, String messageText) {
        // TODO: Buscar workflow asociado al bot/chat
        // TODO: Iniciar workflow con el mensaje como variable
        sendMessage(botId, chatId, "Mensaje recibido: " + messageText);
    }

    private com.javaflow.bot.command.BotCommandHandler getCommandHandler(String command) {
        try {
            // The bean name for a component is its class name, starting with a lowercase letter.
            String beanName = command.substring(1) + "CommandHandler"; // e.g., /start -> startCommandHandler
            return applicationContext.getBean(beanName, com.javaflow.bot.command.BotCommandHandler.class);
        } catch (Exception e) {
            log.warn("No command handler bean found for command: {}", command);
            return null;
        }
    }

    // ========== EVENT LISTENERS ==========

    /**
     * Handles inbound bot messages by listening to BotMessageReceivedEvent.
     * This breaks the circular dependency between TelegramBotAdapter and BotService.
     */
    @EventListener
    @Transactional
    public void handleBotMessageReceived(BotMessageReceivedEvent event) {
        log.debug("Handling BotMessageReceivedEvent for chat {}", event.chatId());
        
        // Record metrics for inbound message
        BotConfiguration bot = getBot(event.botId());
        metricsService.recordBotMessageReceived(bot.getType().name().toLowerCase());
        
        processInboundMessage(
            event.botId(),
            event.chatId(),
            event.userId(),
            event.messageText(),
            event.externalId()
        );
    }
}
