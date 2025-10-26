package com.javaflow.domain.exception;

/**
 * Exception thrown when trying to execute a workflow that is not active
 */
public class WorkflowNotActiveException extends WorkflowDomainException {
    
    public WorkflowNotActiveException(String message) {
        super(message);
    }
}