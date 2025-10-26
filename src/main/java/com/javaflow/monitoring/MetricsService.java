package com.javaflow.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for collecting and recording custom application metrics using Micrometer.
 * 
 * <p>This service provides a centralized way to track business metrics for JavaFlow,
 * including workflow executions, bot interactions, and cache performance. All metrics
 * are exposed via Spring Boot Actuator endpoints and can be scraped by Prometheus.</p>
 * 
 * <p><strong>Available Metrics:</strong></p>
 * <ul>
 *   <li><strong>Workflow Metrics:</strong> Activations, executions (by status), and execution duration</li>
 *   <li><strong>Bot Metrics:</strong> Inbound/outbound messages and command executions</li>
 *   <li><strong>Cache Metrics:</strong> Cache hits and misses by cache name</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * @Service
 * public class MyService {
 *     private final MetricsService metricsService;
 *     
 *     public void executeWorkflow(String workflowName) {
 *         long startTime = System.currentTimeMillis();
 *         try {
 *             // Execute workflow logic
 *             metricsService.recordWorkflowExecution(workflowName, "SUCCESS");
 *         } catch (Exception e) {
 *             metricsService.recordWorkflowExecution(workflowName, "FAILED");
 *         } finally {
 *             long duration = System.currentTimeMillis() - startTime;
 *             metricsService.recordWorkflowExecutionDuration(workflowName, duration);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Accessing Metrics:</strong></p>
 * <ul>
 *   <li>JSON format: {@code http://localhost:8080/actuator/metrics}</li>
 *   <li>Prometheus format: {@code http://localhost:8080/actuator/prometheus}</li>
 *   <li>Specific metric: {@code http://localhost:8080/actuator/metrics/javaflow.workflow.executions}</li>
 * </ul>
 * 
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.boot.actuate.metrics.MetricsEndpoint
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // ========== WORKFLOW METRICS ==========

    /**
     * Records a workflow activation.
     */
    public void recordWorkflowActivation(String workflowName) {
        Counter.builder("javaflow.workflow.activations")
                .tag("workflow", workflowName)
                .description("Number of workflow activations")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a workflow execution.
     */
    public void recordWorkflowExecution(String workflowName, String status) {
        Counter.builder("javaflow.workflow.executions")
                .tag("workflow", workflowName)
                .tag("status", status)
                .description("Number of workflow executions by status")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records the duration of a workflow execution.
     */
    public void recordWorkflowExecutionDuration(String workflowName, long durationMillis) {
        Timer.builder("javaflow.workflow.execution.duration")
                .tag("workflow", workflowName)
                .description("Duration of workflow executions")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }

    // ========== BOT METRICS ==========

    /**
     * Records an inbound bot message.
     */
    public void recordBotMessageReceived(String botType) {
        Counter.builder("javaflow.bot.messages.inbound")
                .tag("bot_type", botType)
                .description("Number of inbound bot messages")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records an outbound bot message.
     */
    public void recordBotMessageSent(String botType) {
        Counter.builder("javaflow.bot.messages.outbound")
                .tag("bot_type", botType)
                .description("Number of outbound bot messages")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a bot command execution.
     */
    public void recordBotCommand(String botType, String command) {
        Counter.builder("javaflow.bot.commands")
                .tag("bot_type", botType)
                .tag("command", command)
                .description("Number of bot commands executed")
                .register(meterRegistry)
                .increment();
    }

    // ========== CACHE METRICS ==========

    /**
     * Records a cache hit.
     */
    public void recordCacheHit(String cacheName) {
        Counter.builder("javaflow.cache.hits")
                .tag("cache", cacheName)
                .description("Number of cache hits")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a cache miss.
     */
    public void recordCacheMiss(String cacheName) {
        Counter.builder("javaflow.cache.misses")
                .tag("cache", cacheName)
                .description("Number of cache misses")
                .register(meterRegistry)
                .increment();
    }
}
