package com.javaflow.application.workflow;

import com.javaflow.application.common.UseCase;
import com.javaflow.application.workflow.command.ActivateWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowResult;
import com.javaflow.model.Workflow;
import com.javaflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for activating a workflow.
 * 
 * This use case handles:
 * 1. Workflow validation
 * 2. BPMN deployment to Flowable engine
 * 3. Status update to ACTIVE
 * 4. Cache invalidation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivateWorkflowUseCase implements UseCase<ActivateWorkflowCommand, WorkflowResult> {

    private final WorkflowRepository workflowRepository;
    private final RepositoryService repositoryService;

    @Override
    @Transactional
    @CacheEvict(value = "workflows", key = "#command.workflowId")
    public WorkflowResult execute(ActivateWorkflowCommand command) {
        log.info("Activating workflow with ID: {}", command.getWorkflowId());
        
        // 1. Load workflow
        Workflow workflow = loadWorkflow(command.getWorkflowId());
        
        // 2. Use domain method to activate (includes business rule validation)
        try {
            workflow.activate();
        } catch (Exception e) {
            log.error("Failed to activate workflow '{}': {}", workflow.getName(), e.getMessage());
            throw new WorkflowActivationException("Failed to activate workflow: " + e.getMessage(), e);
        }
        
        // 3. Deploy to Flowable engine
        deployToFlowableEngine(workflow);
        
        // 4. Save the activated workflow
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        log.info("Workflow '{}' activated successfully", workflow.getName());
        
        return WorkflowResult.from(savedWorkflow);
    }

    private Workflow loadWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + workflowId));
    }

    private void deployToFlowableEngine(Workflow workflow) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .name(workflow.getName())
                    .addString(workflow.getName() + ".bpmn20.xml", workflow.getBpmnXml())
                    .deploy();
            
            log.info("Workflow '{}' deployed to Flowable with deployment ID: {}", 
                    workflow.getName(), deployment.getId());
            
        } catch (Exception e) {
            log.error("Failed to deploy workflow '{}' to Flowable engine", workflow.getName(), e);
            throw new WorkflowDeploymentException(
                "Failed to deploy workflow to process engine: " + e.getMessage(), e
            );
        }
    }

    // Custom exceptions
    public static class WorkflowNotFoundException extends RuntimeException {
        public WorkflowNotFoundException(String message) {
            super(message);
        }
    }

    public static class WorkflowActivationException extends RuntimeException {
        public WorkflowActivationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class WorkflowDeploymentException extends RuntimeException {
        public WorkflowDeploymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}