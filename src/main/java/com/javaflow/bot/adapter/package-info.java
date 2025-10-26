/**
 * Bot platform adapters implementing the Ports & Adapters (Hexagonal) architecture.
 * 
 * <p>This package contains adapter implementations for different bot platforms
 * (Telegram, WhatsApp, etc.). Each adapter implements the {@link com.javaflow.bot.port.BotPort}
 * interface, allowing the core application to communicate with bots without knowing
 * the specific platform details.</p>
 * 
 * <h2>Available Adapters</h2>
 * <ul>
 *   <li>{@link com.javaflow.bot.adapter.TelegramBotAdapter} - Telegram Bot API integration</li>
 *   <li>{@link com.javaflow.bot.adapter.WhatsAppBotAdapter} - WhatsApp integration (placeholder)</li>
 * </ul>
 * 
 * <h2>Architecture Pattern</h2>
 * <p>The Ports & Adapters pattern provides several benefits:</p>
 * <ul>
 *   <li><strong>Platform Independence:</strong> Core logic doesn't depend on specific bot APIs</li>
 *   <li><strong>Testability:</strong> Easy to mock adapters for testing</li>
 *   <li><strong>Flexibility:</strong> Easy to add new bot platforms</li>
 *   <li><strong>Maintainability:</strong> Platform-specific code is isolated</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @Service
 * public class BotService {
 *     private final Map<String, BotPort> botAdapters;
 *     
 *     public void sendMessage(Long botId, String chatId, String content) {
 *         BotConfiguration bot = getBot(botId);
 *         String adapterName = bot.getType().name().toLowerCase() + "BotAdapter";
 *         
 *         // Get the appropriate adapter (telegram, whatsapp, etc.)
 *         BotPort adapter = botAdapters.get(adapterName);
 *         
 *         // Send message using the adapter
 *         adapter.sendMessage(chatId, content);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Adding a New Adapter</h2>
 * <p>To add support for a new bot platform:</p>
 * <ol>
 *   <li>Create a new class implementing {@link com.javaflow.bot.port.BotPort}</li>
 *   <li>Annotate with {@code @Component} and name it "{platform}BotAdapter"</li>
 *   <li>Implement the three required methods: {@code sendMessage}, {@code sendMessageWithButtons}, {@code getBotType}</li>
 *   <li>Add platform-specific configuration if needed</li>
 * </ol>
 * 
 * @see com.javaflow.bot.port.BotPort
 * @see com.javaflow.service.BotService
 * @since 1.0.0
 */
package com.javaflow.bot.adapter;
