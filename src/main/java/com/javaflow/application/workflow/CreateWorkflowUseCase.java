package com.javaflow.application.workflow;

import com.javaflow.application.common.UseCase;
import com.javaflow.application.workflow.command.CreateWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowResult;
import com.javaflow.domain.service.WorkflowValidationService;
import com.javaflow.model.User;
import com.javaflow.model.Workflow;
import com.javaflow.repository.UserRepository;
import com.javaflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new workflow.
 * 
 * This use case handles:
 * 1. Input validation
 * 2. User verification
 * 3. Workflow creation with proper defaults
 * 4. Cache invalidation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateWorkflowUseCase implements UseCase<CreateWorkflowCommand, WorkflowResult> {

    private final WorkflowRepository workflowRepository;
    private final UserRepository userRepository;
    private final WorkflowValidationService validationService;

    @Override
    @Transactional
    @CacheEvict(value = "workflows", allEntries = true)
    public WorkflowResult execute(CreateWorkflowCommand command) {
        log.info("Creating workflow: {}", command.getName());
        
        // 1. Validate input
        validateCommand(command);
        
        // 2. Load user
        User createdBy = loadUser(command.getCreatedByUserId());
        
        // 3. Create workflow
        Workflow workflow = createWorkflow(command, createdBy);
        
        // 4. Save and return result
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        log.info("Workflow created successfully with ID: {}", savedWorkflow.getId());
        
        return WorkflowResult.from(savedWorkflow);
    }

    private void validateCommand(CreateWorkflowCommand command) {
        // Basic input validation
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new InvalidWorkflowDataException("Workflow name is required");
        }
        
        if (command.getBpmnXml() == null || command.getBpmnXml().trim().isEmpty()) {
            throw new InvalidWorkflowDataException("BPMN XML is required");
        }
        
        // Check if workflow name already exists
        if (workflowRepository.findByName(command.getName()).isPresent()) {
            throw new WorkflowAlreadyExistsException(
                "Workflow with name '" + command.getName() + "' already exists"
            );
        }
        
        // Use domain service for BPMN validation
        WorkflowValidationService.ValidationResult bpmnValidation = 
            validationService.validateBpmn(command.getBpmnXml());
        
        if (!bpmnValidation.isValid()) {
            throw new InvalidWorkflowDataException(
                "Invalid BPMN XML: " + bpmnValidation.getErrorMessage()
            );
        }
        
        if (bpmnValidation.hasWarnings()) {
            log.warn("BPMN validation warnings for workflow '{}': {}", 
                command.getName(), bpmnValidation.getWarningMessage());
        }
    }

    private User loadUser(Long userId) {
        if (userId == null) {
            throw new InvalidWorkflowDataException("Created by user ID is required");
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private Workflow createWorkflow(CreateWorkflowCommand command, User createdBy) {
        Workflow workflow = Workflow.builder()
                .name(command.getName().trim())
                .description(command.getDescription())
                .bpmnXml(command.getBpmnXml())
                .status(Workflow.WorkflowStatus.DRAFT)
                .createdBy(createdBy)
                .build();
        
        // Use domain service for business rule validation
        WorkflowValidationService.ValidationResult businessValidation = 
            validationService.validateWorkflowBusinessRules(workflow);
        
        if (!businessValidation.isValid()) {
            throw new InvalidWorkflowDataException(
                "Workflow business rules validation failed: " + businessValidation.getErrorMessage()
            );
        }
        
        if (businessValidation.hasWarnings()) {
            log.warn("Workflow business rules warnings for '{}': {}", 
                workflow.getName(), businessValidation.getWarningMessage());
        }
        
        return workflow;
    }

    // Custom exceptions
    public static class InvalidWorkflowDataException extends RuntimeException {
        public InvalidWorkflowDataException(String message) {
            super(message);
        }
    }

    public static class WorkflowAlreadyExistsException extends RuntimeException {
        public WorkflowAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}