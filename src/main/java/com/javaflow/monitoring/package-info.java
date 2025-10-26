/**
 * Monitoring and observability services for JavaFlow.
 * 
 * <p>This package provides comprehensive monitoring capabilities using Micrometer
 * and Spring Boot Actuator. It enables tracking of business metrics, performance
 * monitoring, and integration with monitoring systems like Prometheus and Grafana.</p>
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link com.javaflow.monitoring.MetricsService} - Custom business metrics collection</li>
 * </ul>
 * 
 * <h2>Available Metrics</h2>
 * 
 * <h3>Workflow Metrics</h3>
 * <ul>
 *   <li>{@code javaflow.workflow.activations} - Count of workflow activations</li>
 *   <li>{@code javaflow.workflow.executions} - Count of executions by status (SUCCESS/FAILED)</li>
 *   <li>{@code javaflow.workflow.execution.duration} - Execution duration in milliseconds</li>
 * </ul>
 * 
 * <h3>Bot Metrics</h3>
 * <ul>
 *   <li>{@code javaflow.bot.messages.inbound} - Inbound messages by bot type</li>
 *   <li>{@code javaflow.bot.messages.outbound} - Outbound messages by bot type</li>
 *   <li>{@code javaflow.bot.commands} - Bot commands executed</li>
 * </ul>
 * 
 * <h3>Cache Metrics</h3>
 * <ul>
 *   <li>{@code javaflow.cache.hits} - Cache hits by cache name</li>
 *   <li>{@code javaflow.cache.misses} - Cache misses by cache name</li>
 * </ul>
 * 
 * <h2>Accessing Metrics</h2>
 * <p>Metrics are exposed via Spring Boot Actuator endpoints:</p>
 * <ul>
 *   <li><strong>Health:</strong> {@code http://localhost:8080/actuator/health}</li>
 *   <li><strong>Metrics (JSON):</strong> {@code http://localhost:8080/actuator/metrics}</li>
 *   <li><strong>Prometheus:</strong> {@code http://localhost:8080/actuator/prometheus}</li>
 *   <li><strong>Specific Metric:</strong> {@code http://localhost:8080/actuator/metrics/{metric.name}}</li>
 * </ul>
 * 
 * <h2>Integration with Prometheus</h2>
 * <p>Configure Prometheus to scrape metrics:</p>
 * <pre>
 * # prometheus.yml
 * scrape_configs:
 *   - job_name: 'javaflow'
 *     metrics_path: '/actuator/prometheus'
 *     static_configs:
 *       - targets: ['localhost:8080']
 * </pre>
 * 
 * <h2>Standard JVM Metrics</h2>
 * <p>In addition to custom metrics, the following standard metrics are available:</p>
 * <ul>
 *   <li>JVM memory usage and garbage collection</li>
 *   <li>HTTP request metrics (count, duration, status codes)</li>
 *   <li>Database connection pool metrics (HikariCP)</li>
 *   <li>Thread pool metrics</li>
 *   <li>Logback metrics</li>
 * </ul>
 * 
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration
 * @since 1.0.0
 */
package com.javaflow.monitoring;
