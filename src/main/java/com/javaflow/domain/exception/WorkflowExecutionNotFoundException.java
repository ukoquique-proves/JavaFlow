package com.javaflow.domain.exception;

/**
 * Exception thrown when a workflow execution is not found.
 * 
 * @since 1.0.0
 */
public class WorkflowExecutionNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new WorkflowExecutionNotFoundException with the execution ID.
     *
     * @param executionId The ID of the execution that was not found
     */
    public WorkflowExecutionNotFoundException(Long executionId) {
        super(String.format("Workflow execution with id %d not found", executionId));
    }
    
    /**
     * Constructs a new WorkflowExecutionNotFoundException for a process instance.
     *
     * @param processInstanceId The process instance ID that was not found
     * @param isProcessInstance Flag to differentiate constructor signature
     */
    public WorkflowExecutionNotFoundException(String processInstanceId, boolean isProcessInstance) {
        super(String.format("Workflow execution not found for process instance: %s", processInstanceId));
    }
    
    /**
     * Constructs a new WorkflowExecutionNotFoundException with a custom message.
     *
     * @param message The detail message
     */
    public WorkflowExecutionNotFoundException(String message) {
        super(message);
    }
}
