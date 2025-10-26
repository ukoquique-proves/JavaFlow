package com.javaflow.domain.exception;

/**
 * Base exception for workflow domain rule violations
 */
public abstract class WorkflowDomainException extends RuntimeException {
    
    protected WorkflowDomainException(String message) {
        super(message);
    }
    
    protected WorkflowDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}