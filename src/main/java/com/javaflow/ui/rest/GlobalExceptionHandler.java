package com.javaflow.ui.rest;

import com.javaflow.application.workflow.ActivateWorkflowUseCase;
import com.javaflow.application.workflow.CreateWorkflowUseCase;
import com.javaflow.application.workflow.ExecuteWorkflowUseCase;
import com.javaflow.domain.exception.WorkflowDomainException;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API
 * 
 * Provides consistent error response format and proper HTTP status codes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle domain exceptions (business rule violations)
     */
    @ExceptionHandler(WorkflowDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(WorkflowDomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getMessage())
                .path("/api/v1/workflows")
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle use case specific exceptions
     */
    @ExceptionHandler({
        ExecuteWorkflowUseCase.WorkflowNotFoundException.class,
        ActivateWorkflowUseCase.WorkflowNotFoundException.class,
        CreateWorkflowUseCase.UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException ex) {
        log.warn("Not found exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path("/api/v1/workflows")
                .build();
        
        return ResponseEntity.notFound().build();
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path("/api/v1/workflows")
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle workflow execution exceptions
     */
    @ExceptionHandler({
        ExecuteWorkflowUseCase.WorkflowExecutionException.class,
        ActivateWorkflowUseCase.WorkflowDeploymentException.class
    })
    public ResponseEntity<ErrorResponse> handleExecutionException(RuntimeException ex) {
        log.error("Execution exception: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Execution Failed")
                .message(ex.getMessage())
                .path("/api/v1/workflows")
                .build();
        
        return ResponseEntity.internalServerError().body(error);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path("/api/v1/workflows")
                .build();
        
        return ResponseEntity.internalServerError().body(error);
    }

    /**
     * Standard error response DTO
     */
    @Value
    @Builder
    public static class ErrorResponse {
        LocalDateTime timestamp;
        int status;
        String error;
        String message;
        String path;
    }

    /**
     * Validation error response DTO with field-specific errors
     */
    @Value
    @Builder
    public static class ValidationErrorResponse {
        LocalDateTime timestamp;
        int status;
        String error;
        String message;
        String path;
        Map<String, String> fieldErrors;
    }
}