package com.javaflow.application.workflow.result;

import com.javaflow.model.Workflow;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Result for workflow operations (create, activate, etc.)
 */
@Value
@Builder
public class WorkflowResult {
    
    Long id;
    String name;
    String description;
    Workflow.WorkflowStatus status;
    Integer version;
    LocalDateTime createdAt;
    String createdByUsername;
    
    public static WorkflowResult from(Workflow workflow) {
        return WorkflowResult.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .status(workflow.getStatus())
                .version(workflow.getVersion())
                .createdAt(workflow.getCreatedAt())
                .createdByUsername(workflow.getCreatedBy() != null ? 
                    workflow.getCreatedBy().getUsername() : "System")
                .build();
    }
}