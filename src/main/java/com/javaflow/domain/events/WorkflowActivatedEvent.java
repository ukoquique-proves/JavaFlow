package com.javaflow.domain.events;

import java.time.LocalDateTime;

/**
 * An event that is published when a workflow is successfully activated.
 *
 * @param workflowId   The ID of the activated workflow.
 * @param workflowName The name of the activated workflow.
 * @param activatedAt  The timestamp when the activation occurred.
 */
public record WorkflowActivatedEvent(
        Long workflowId,
        String workflowName,
        LocalDateTime activatedAt
) {
}
