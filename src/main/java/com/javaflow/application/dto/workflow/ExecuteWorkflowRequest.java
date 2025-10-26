package com.javaflow.application.dto.workflow;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Request DTO for executing a workflow
 */
@Value
@Builder
public class ExecuteWorkflowRequest {
    
    @NotNull(message = "Workflow ID is required")
    Long workflowId;
    
    Map<String, Object> variables;
    
    Long startedByUserId;
    
    public static ExecuteWorkflowRequest of(Long workflowId, Map<String, Object> variables, Long userId) {
        return ExecuteWorkflowRequest.builder()
                .workflowId(workflowId)
                .variables(variables != null ? variables : Map.of())
                .startedByUserId(userId)
                .build();
    }
}