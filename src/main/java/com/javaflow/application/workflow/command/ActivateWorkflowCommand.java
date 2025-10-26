package com.javaflow.application.workflow.command;

import com.javaflow.application.common.Command;
import lombok.Builder;
import lombok.Value;

/**
 * Command to activate a workflow
 */
@Value
@Builder
public class ActivateWorkflowCommand implements Command {
    
    Long workflowId;
    
    public static ActivateWorkflowCommand of(Long workflowId) {
        return ActivateWorkflowCommand.builder()
                .workflowId(workflowId)
                .build();
    }
}