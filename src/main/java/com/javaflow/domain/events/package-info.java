/**
 * Domain events representing significant business occurrences in JavaFlow.
 * 
 * <p>This package contains domain event records that are published when important
 * business actions occur. Events follow the Domain-Driven Design (DDD) pattern and
 * enable loose coupling between different parts of the application.</p>
 * 
 * <h2>Available Events</h2>
 * <ul>
 *   <li>{@link com.javaflow.domain.events.WorkflowActivatedEvent} - Published when a workflow is activated</li>
 *   <li>{@link com.javaflow.domain.events.WorkflowExecutedEvent} - Published when a workflow execution completes</li>
 *   <li>{@link com.javaflow.domain.events.BotMessageReceivedEvent} - Published when a bot receives a message</li>
 * </ul>
 * 
 * <h2>Event Publishing</h2>
 * <p>Events are published using Spring's {@code ApplicationEventPublisher}:</p>
 * <pre>{@code
 * // In a domain entity or service
 * public void activate() {
 *     this.status = WorkflowStatus.ACTIVE;
 *     
 *     // Publish domain event
 *     WorkflowActivatedEvent event = new WorkflowActivatedEvent(
 *         this.id,
 *         this.name,
 *         LocalDateTime.now()
 *     );
 *     
 *     // Event will be published by the infrastructure layer
 *     this.domainEvents.add(event);
 * }
 * }</pre>
 * 
 * <h2>Event Handling</h2>
 * <p>Events are handled asynchronously by event handlers:</p>
 * <pre>{@code
 * @Component
 * public class MyEventHandler {
 *     
 *     @EventListener
 *     @Async
 *     public void handle(WorkflowActivatedEvent event) {
 *         // Handle the event (logging, metrics, notifications, etc.)
 *         log.info("Workflow activated: {}", event.workflowName());
 *     }
 * }
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Immutable:</strong> All events are immutable Java records</li>
 *   <li><strong>Self-describing:</strong> Event names clearly describe what happened</li>
 *   <li><strong>Past tense:</strong> Events represent facts that already occurred</li>
 *   <li><strong>Rich data:</strong> Events contain all relevant information</li>
 * </ul>
 * 
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.event.EventListener
 * @see com.javaflow.application.handlers.WorkflowEventHandler
 * @since 1.0.0
 */
package com.javaflow.domain.events;
