package com.javaflow.domain.exception;

/**
 * Exception thrown when trying to activate a workflow that is already active
 */
public class WorkflowAlreadyActiveException extends WorkflowDomainException {
    
    public WorkflowAlreadyActiveException(String message) {
        super(message);
    }
}