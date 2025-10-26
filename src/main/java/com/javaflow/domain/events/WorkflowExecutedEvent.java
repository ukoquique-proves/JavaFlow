package com.javaflow.domain.events;

import com.javaflow.model.WorkflowExecution.ExecutionStatus;

import java.time.LocalDateTime;

/**
 * An event that is published when a workflow execution is created.
 *
 * @param executionId The ID of the workflow execution.
 * @param workflowId  The ID of the parent workflow.
 * @param status      The initial status of the execution (e.g., RUNNING).
 * @param executedAt  The timestamp when the execution started.
 */
public record WorkflowExecutedEvent(
        Long executionId,
        Long workflowId,
        ExecutionStatus status,
        LocalDateTime executedAt
) {
}
