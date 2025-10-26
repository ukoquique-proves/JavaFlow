# JavaFlow Metrics Guide

## Overview

JavaFlow uses Micrometer for comprehensive application monitoring and metrics collection. All metrics are exposed via Spring Boot Actuator endpoints.

## Accessing Metrics

### Metrics Endpoint
```bash
# List all available metrics
curl http://localhost:8080/actuator/metrics

# View a specific metric
curl http://localhost:8080/actuator/metrics/{metric.name}
```

### Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

## Custom JavaFlow Metrics

### Workflow Metrics

#### `javaflow.workflow.activations`
- **Type**: Counter
- **Description**: Number of workflow activations
- **Tags**: `workflow` (workflow name)
- **Example**:
  ```bash
  curl http://localhost:8080/actuator/metrics/javaflow.workflow.activations
  ```

#### `javaflow.workflow.executions`
- **Type**: Counter
- **Description**: Number of workflow executions by status
- **Tags**: `workflow` (workflow name), `status` (RUNNING, COMPLETED, FAILED, etc.)
- **Example**:
  ```bash
  curl http://localhost:8080/actuator/metrics/javaflow.workflow.executions
  ```

#### `javaflow.workflow.execution.duration`
- **Type**: Timer
- **Description**: Duration of workflow executions in milliseconds
- **Tags**: `workflow` (workflow name)

### Bot Metrics

#### `javaflow.bot.messages.inbound`
- **Type**: Counter
- **Description**: Number of inbound bot messages
- **Tags**: `bot_type` (telegram, whatsapp)
- **Example**:
  ```bash
  curl http://localhost:8080/actuator/metrics/javaflow.bot.messages.inbound
  ```

#### `javaflow.bot.messages.outbound`
- **Type**: Counter
- **Description**: Number of outbound bot messages
- **Tags**: `bot_type` (telegram, whatsapp)

#### `javaflow.bot.commands`
- **Type**: Counter
- **Description**: Number of bot commands executed
- **Tags**: `bot_type` (telegram, whatsapp), `command` (start, help, status, workflows, unknown)
- **Example**:
  ```bash
  curl http://localhost:8080/actuator/metrics/javaflow.bot.commands
  ```

### Cache Metrics

#### `javaflow.cache.hits`
- **Type**: Counter
- **Description**: Number of cache hits
- **Tags**: `cache` (cache name)

#### `javaflow.cache.misses`
- **Type**: Counter
- **Description**: Number of cache misses
- **Tags**: `cache` (cache name)

## Standard Spring Boot Metrics

JavaFlow also exposes all standard Spring Boot metrics:

### JVM Metrics
- `jvm.memory.used` - JVM memory usage
- `jvm.gc.pause` - Garbage collection pauses
- `jvm.threads.live` - Number of live threads
- `jvm.classes.loaded` - Number of loaded classes

### HTTP Metrics
- `http.server.requests` - HTTP request metrics (count, duration, percentiles)
- `http.server.requests.active` - Active HTTP requests

### Database Metrics
- `hikaricp.connections.active` - Active database connections
- `hikaricp.connections.idle` - Idle database connections
- `jdbc.connections.max` - Maximum database connections

### Cache Metrics (Caffeine)
- `cache.gets` - Cache get operations
- `cache.puts` - Cache put operations
- `cache.evictions` - Cache evictions
- `cache.size` - Current cache size

## Metric Tags

All custom metrics include these global tags:
- `application`: JavaFlow
- `environment`: dev (or the active Spring profile)

## Integration with Monitoring Systems

### Prometheus
The metrics are compatible with Prometheus. The endpoint is available at:
```
http://localhost:8080/actuator/prometheus
```

### Grafana
You can create Grafana dashboards using these metrics. Example queries:

**Workflow Activation Rate**:
```promql
rate(javaflow_workflow_activations_total[5m])
```

**Bot Message Throughput**:
```promql
rate(javaflow_bot_messages_inbound_total[5m])
```

**Workflow Execution Success Rate**:
```promql
sum(rate(javaflow_workflow_executions_total{status="COMPLETED"}[5m])) 
/ 
sum(rate(javaflow_workflow_executions_total[5m]))
```

## Configuration

Metrics configuration is in `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      simple:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

## Best Practices

1. **Monitor Workflow Execution Duration**: Set up alerts for workflows that take longer than expected
2. **Track Bot Message Volume**: Monitor message rates to detect unusual activity
3. **Watch Cache Hit Rates**: Low cache hit rates may indicate configuration issues
4. **Monitor JVM Memory**: Set up alerts for high memory usage to prevent OutOfMemoryErrors
5. **Track HTTP Request Latency**: Monitor p95 and p99 latencies for performance issues

## Troubleshooting

### Metrics Not Appearing
- Ensure the operation has been triggered at least once
- Check that the `MetricsService` is being injected correctly
- Verify the actuator endpoint is accessible

### High Memory Usage
- Check `jvm.memory.used` metric
- Review cache sizes with `cache.size` metric
- Monitor garbage collection with `jvm.gc.pause`

### Slow Performance
- Check `http.server.requests` for request latencies
- Review `javaflow.workflow.execution.duration` for slow workflows
- Monitor database connection pool with `hikaricp.connections.*`
