package com.javaflow.domain.exception;

/**
 * Exception thrown when a workflow cannot be activated due to business rules
 */
public class WorkflowCannotBeActivatedException extends WorkflowDomainException {
    
    public WorkflowCannotBeActivatedException(String message) {
        super(message);
    }
}