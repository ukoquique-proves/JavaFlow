package com.javaflow.domain.exception;

/**
 * Exception thrown when workflow execution operations violate business rules
 */
public class WorkflowExecutionException extends WorkflowDomainException {
    
    public WorkflowExecutionException(String message) {
        super(message);
    }
}