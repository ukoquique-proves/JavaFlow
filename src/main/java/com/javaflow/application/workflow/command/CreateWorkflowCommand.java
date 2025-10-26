package com.javaflow.application.workflow.command;

import com.javaflow.application.common.Command;
import lombok.Builder;
import lombok.Value;

/**
 * Command to create a new workflow
 */
@Value
@Builder
public class CreateWorkflowCommand implements Command {
    
    String name;
    String description;
    String bpmnXml;
    Long createdByUserId;
    
    public static CreateWorkflowCommand of(String name, String description, String bpmnXml, Long userId) {
        return CreateWorkflowCommand.builder()
                .name(name)
                .description(description)
                .bpmnXml(bpmnXml)
                .createdByUserId(userId)
                .build();
    }
}