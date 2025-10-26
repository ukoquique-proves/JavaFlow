package com.javaflow.domain.exception;

/**
 * Exception thrown when a workflow is not found.
 * 
 * @since 1.0.0
 */
public class WorkflowNotFoundException extends WorkflowDomainException {
    
    /**
     * Constructs a new WorkflowNotFoundException with the workflow ID.
     *
     * @param workflowId The ID of the workflow that was not found
     */
    public WorkflowNotFoundException(Long workflowId) {
        super(String.format("Workflow with id %d not found", workflowId));
    }
    
    /**
     * Constructs a new WorkflowNotFoundException with a custom message.
     *
     * @param message The detail message
     */
    public WorkflowNotFoundException(String message) {
        super(message);
    }
}
