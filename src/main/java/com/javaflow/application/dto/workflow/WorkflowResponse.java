package com.javaflow.application.dto.workflow;

import com.javaflow.model.Workflow;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response DTO for workflow operations
 */
@Value
@Builder
public class WorkflowResponse {
    
    Long id;
    String name;
    String description;
    String status;
    Integer version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    
    // Statistics
    Integer executionCount;
    Long successfulExecutions;
    Long failedExecutions;
    Double successRate;
    
    public static WorkflowResponse from(Workflow workflow) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .status(workflow.getStatus().toString())
                .version(workflow.getVersion())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .createdBy(workflow.getCreatedBy() != null ? workflow.getCreatedBy().getUsername() : "System")
                .executionCount(workflow.getExecutionCount())
                .successfulExecutions(workflow.getSuccessfulExecutionCount())
                .failedExecutions(workflow.getFailedExecutionCount())
                .successRate(workflow.getSuccessRate())
                .build();
    }
    
    public static WorkflowResponse fromBasic(Workflow workflow) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .status(workflow.getStatus().toString())
                .version(workflow.getVersion())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .createdBy(workflow.getCreatedBy() != null ? workflow.getCreatedBy().getUsername() : "System")
                .build();
    }
}