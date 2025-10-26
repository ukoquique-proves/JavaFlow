package com.javaflow.application.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * Request DTO for creating a new workflow
 */
@Value
@Builder
public class CreateWorkflowRequest {
    
    @NotBlank(message = "Workflow name is required")
    @Size(min = 3, max = 100, message = "Workflow name must be between 3 and 100 characters")
    String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description;
    
    @NotBlank(message = "BPMN XML is required")
    String bpmnXml;
    
    public static CreateWorkflowRequest of(String name, String description, String bpmnXml) {
        return CreateWorkflowRequest.builder()
                .name(name)
                .description(description)
                .bpmnXml(bpmnXml)
                .build();
    }
}