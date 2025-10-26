package com.javaflow.application.dto.workflow;

import com.javaflow.model.WorkflowExecution;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response DTO for workflow execution operations
 */
@Value
@Builder
public class WorkflowExecutionResponse {
    
    Long id;
    Long workflowId;
    String workflowName;
    String processInstanceId;
    String status;
    String statusDescription;
    LocalDateTime startedAt;
    LocalDateTime endedAt;
    String startedBy;
    String errorMessage;
    Long durationMillis;
    
    public static WorkflowExecutionResponse from(WorkflowExecution execution) {
        return WorkflowExecutionResponse.builder()
                .id(execution.getId())
                .workflowId(execution.getWorkflow().getId())
                .workflowName(execution.getWorkflow().getName())
                .processInstanceId(execution.getProcessInstanceId())
                .status(execution.getStatus().toString())
                .statusDescription(execution.getStatusDescription())
                .startedAt(execution.getStartedAt())
                .endedAt(execution.getEndedAt())
                .startedBy(execution.getStartedBy() != null ? execution.getStartedBy().getUsername() : "System")
                .errorMessage(execution.getErrorMessage())
                .durationMillis(execution.getDurationMillis())
                .build();
    }
}