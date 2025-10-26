package com.javaflow.application.workflow;

import com.javaflow.application.common.UseCase;
import com.javaflow.application.workflow.command.ExecuteWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowExecutionResult;
import com.javaflow.model.User;
import com.javaflow.model.Workflow;
import com.javaflow.model.WorkflowExecution;
import com.javaflow.repository.UserRepository;
import com.javaflow.repository.WorkflowExecutionRepository;
import com.javaflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for executing a workflow.
 * 
 * This use case orchestrates the workflow execution process:
 * 1. Validates the workflow exists and is active
 * 2. Validates the user exists
 * 3. Starts the process instance in Flowable
 * 4. Creates and persists the execution record
 * 5. Returns the execution result
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecuteWorkflowUseCase implements UseCase<ExecuteWorkflowCommand, WorkflowExecutionResult> {

    private final WorkflowRepository workflowRepository;
    private final UserRepository userRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final RuntimeService runtimeService;

    @Override
    @Transactional
    public WorkflowExecutionResult execute(ExecuteWorkflowCommand command) {
        log.info("Executing workflow use case for workflow ID: {}", command.getWorkflowId());
        
        // 1. Validate and load workflow
        Workflow workflow = loadAndValidateWorkflow(command.getWorkflowId());
        
        // 2. Load user if provided
        User startedBy = loadUser(command.getStartedByUserId());
        
        // 3. Start process instance in Flowable
        ProcessInstance processInstance = startProcessInstance(workflow, command.getVariables());
        
        // 4. Create and persist execution record
        WorkflowExecution execution = createExecutionRecord(workflow, processInstance, startedBy);
        
        // 5. Return result
        WorkflowExecutionResult result = WorkflowExecutionResult.from(execution);
        
        log.info("Workflow execution started successfully. Execution ID: {}, Process Instance ID: {}", 
                result.getExecutionId(), result.getProcessInstanceId());
        
        return result;
    }

    private Workflow loadAndValidateWorkflow(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + workflowId));
        
        // Use domain method for validation
        if (!workflow.canBeExecuted()) {
            throw new WorkflowNotActiveException(
                String.format("Workflow '%s' cannot be executed. Current status: %s", 
                    workflow.getName(), workflow.getStatus())
            );
        }
        
        return workflow;
    }

    private User loadUser(Long userId) {
        if (userId == null) {
            return null; // System execution
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private ProcessInstance startProcessInstance(Workflow workflow, java.util.Map<String, Object> variables) {
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    workflow.getName(),
                    variables
            );
            
            log.debug("Process instance started: {} for workflow: {}", 
                    processInstance.getId(), workflow.getName());
            
            return processInstance;
        } catch (Exception e) {
            log.error("Failed to start process instance for workflow: {}", workflow.getName(), e);
            throw new WorkflowExecutionException(
                "Failed to start workflow execution: " + e.getMessage(), e
            );
        }
    }

    private WorkflowExecution createExecutionRecord(Workflow workflow, ProcessInstance processInstance, User startedBy) {
        // Use domain method to create execution (includes business rule validation)
        WorkflowExecution execution = workflow.createExecution(java.util.Map.of(), startedBy);
        execution.setProcessInstanceId(processInstance.getId());
        
        return executionRepository.save(execution);
    }

    // Custom exceptions for better error handling
    public static class WorkflowNotFoundException extends RuntimeException {
        public WorkflowNotFoundException(String message) {
            super(message);
        }
    }

    public static class WorkflowNotActiveException extends RuntimeException {
        public WorkflowNotActiveException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class WorkflowExecutionException extends RuntimeException {
        public WorkflowExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}