package com.javaflow.application.workflow.result;

import com.javaflow.model.WorkflowExecution;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Result of workflow execution use case
 */
@Value
@Builder
public class WorkflowExecutionResult {
    
    Long executionId;
    Long workflowId;
    String workflowName;
    String processInstanceId;
    WorkflowExecution.ExecutionStatus status;
    LocalDateTime startedAt;
    String startedByUsername;
    
    public static WorkflowExecutionResult from(WorkflowExecution execution) {
        return WorkflowExecutionResult.builder()
                .executionId(execution.getId())
                .workflowId(execution.getWorkflow().getId())
                .workflowName(execution.getWorkflow().getName())
                .processInstanceId(execution.getProcessInstanceId())
                .status(execution.getStatus())
                .startedAt(execution.getStartedAt())
                .startedByUsername(execution.getStartedBy() != null ? 
                    execution.getStartedBy().getUsername() : "System")
                .build();
    }
}