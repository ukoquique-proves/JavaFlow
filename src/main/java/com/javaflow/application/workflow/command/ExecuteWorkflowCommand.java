package com.javaflow.application.workflow.command;

import com.javaflow.application.common.Command;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Command to execute a workflow with given variables
 */
@Value
@Builder
public class ExecuteWorkflowCommand implements Command {
    
    Long workflowId;
    Map<String, Object> variables;
    Long startedByUserId;
    
    public static ExecuteWorkflowCommand of(Long workflowId, Map<String, Object> variables, Long userId) {
        return ExecuteWorkflowCommand.builder()
                .workflowId(workflowId)
                .variables(variables != null ? variables : Map.of())
                .startedByUserId(userId)
                .build();
    }
}