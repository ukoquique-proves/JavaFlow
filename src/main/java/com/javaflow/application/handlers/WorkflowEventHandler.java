package com.javaflow.application.handlers;

import com.javaflow.domain.events.WorkflowActivatedEvent;
import com.javaflow.domain.events.WorkflowExecutedEvent;
import com.javaflow.monitoring.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event handler for workflow-related domain events.
 * Handles logging, metrics collection, and other cross-cutting concerns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventHandler {

    private final MetricsService metricsService;

    /**
     * Handles the event when a workflow is activated.
     * This method is executed asynchronously to avoid blocking the main thread.
     *
     * @param event The workflow activation event.
     */
    @EventListener
    @Async
    public void handle(WorkflowActivatedEvent event) {
        log.info("✅ Workflow Activated Event Received: Name='{}', ID={}", event.workflowName(), event.workflowId());
        
        // Record metrics
        metricsService.recordWorkflowActivation(event.workflowName());
        
        // TODO: Add logic for notifications or audit trails here
    }

    /**
     * Handles the event when a workflow is executed.
     *
     * @param event The workflow execution event.
     */
    @EventListener
    public void handle(WorkflowExecutedEvent event) {
        log.info("🚀 Workflow Executed Event Received: WorkflowID={}, Status={}", event.workflowId(), event.status());
        
        // Record metrics
        metricsService.recordWorkflowExecution("workflow-" + event.workflowId(), event.status().name());
        
        // TODO: Update execution statistics or trigger dependent workflows
    }
}
