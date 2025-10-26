package com.javaflow.service;

import com.javaflow.application.workflow.ActivateWorkflowUseCase;
import com.javaflow.application.workflow.CreateWorkflowUseCase;
import com.javaflow.application.workflow.ExecuteWorkflowUseCase;
import com.javaflow.application.workflow.command.ActivateWorkflowCommand;
import com.javaflow.application.workflow.command.CreateWorkflowCommand;
import com.javaflow.application.workflow.command.ExecuteWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowExecutionResult;
import com.javaflow.application.workflow.result.WorkflowResult;
import com.javaflow.model.Workflow;
import com.javaflow.model.WorkflowExecution;
import com.javaflow.model.User;
import com.javaflow.repository.WorkflowRepository;
import com.javaflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de Workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final com.javaflow.monitoring.MetricsService metricsService;
    
    // Use Cases
    private final CreateWorkflowUseCase createWorkflowUseCase;
    private final ActivateWorkflowUseCase activateWorkflowUseCase;
    private final ExecuteWorkflowUseCase executeWorkflowUseCase;

    /**
     * Crear un nuevo workflow
     * @deprecated Use CreateWorkflowUseCase directly
     */
    @Deprecated
    @Transactional
    @CacheEvict(value = "workflows", allEntries = true)
    public Workflow createWorkflow(String name, String description, String bpmnXml, User createdBy) {
        log.warn("Using deprecated createWorkflow method. Consider using CreateWorkflowUseCase directly.");
        
        CreateWorkflowCommand command = CreateWorkflowCommand.of(name, description, bpmnXml, createdBy.getId());
        WorkflowResult result = createWorkflowUseCase.execute(command);
        
        // Return the entity for backward compatibility
        return getWorkflow(result.getId());
    }
    
    /**
     * Create workflow using use case pattern
     */
    public WorkflowResult createWorkflowWithUseCase(String name, String description, String bpmnXml, Long userId) {
        CreateWorkflowCommand command = CreateWorkflowCommand.of(name, description, bpmnXml, userId);
        return createWorkflowUseCase.execute(command);
    }

    /**
     * Obtener workflow por ID
     */
    @Cacheable(value = "workflows", key = "#id")
    public Workflow getWorkflow(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new com.javaflow.domain.exception.WorkflowNotFoundException(id));
    }

    /**
     * Listar todos los workflows
     */
    @Cacheable(value = "workflows")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }
    
    /**
     * Listar todos los workflows con creador (optimizado - previene N+1)
     */
    @Cacheable(value = "workflows", key = "'all-with-creator'")
    public List<Workflow> getAllWorkflowsWithCreator() {
        return workflowRepository.findAllWithCreator();
    }
    
    /**
     * Listar todos los workflows con ejecuciones (optimizado para dashboard)
     */
    @Cacheable(value = "workflows", key = "'all-with-executions'")
    public List<Workflow> getAllWorkflowsWithExecutions() {
        return workflowRepository.findAllWithExecutions();
    }

    /**
     * Listar workflows por usuario
     */
    public List<Workflow> getWorkflowsByUser(Long userId) {
        return workflowRepository.findByCreatedById(userId);
    }

    /**
     * Activar un workflow (desplegarlo en Flowable)
     * @deprecated Use ActivateWorkflowUseCase directly
     */
    @Deprecated
    @Transactional
    @CacheEvict(value = "workflows", key = "#id")
    public Workflow activateWorkflow(Long id) {
        log.warn("Using deprecated activateWorkflow method. Consider using ActivateWorkflowUseCase directly.");
        
        ActivateWorkflowCommand command = ActivateWorkflowCommand.of(id);
        WorkflowResult result = activateWorkflowUseCase.execute(command);
        
        // Return the entity for backward compatibility
        return getWorkflow(result.getId());
    }
    
    /**
     * Activate workflow using use case pattern
     */
    public WorkflowResult activateWorkflowWithUseCase(Long id) {
        ActivateWorkflowCommand command = ActivateWorkflowCommand.of(id);
        return activateWorkflowUseCase.execute(command);
    }

    /**
     * Desactivar un workflow
     */
    @Transactional
    @CacheEvict(value = "workflows", key = "#id")
    public Workflow deactivateWorkflow(Long id) {
        log.info("Deactivating workflow: {}", id);
        
        Workflow workflow = getWorkflow(id);
        workflow.setStatus(Workflow.WorkflowStatus.INACTIVE);
        return workflowRepository.save(workflow);
    }

    /**
     * Ejecutar un workflow
     * @deprecated Use ExecuteWorkflowUseCase directly
     */
    @Deprecated
    @Transactional
    public WorkflowExecution executeWorkflow(Long workflowId, Map<String, Object> variables, User startedBy) {
        log.warn("Using deprecated executeWorkflow method. Consider using ExecuteWorkflowUseCase directly.");
        
        Long userId = startedBy != null ? startedBy.getId() : null;
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(workflowId, variables, userId);
        WorkflowExecutionResult result = executeWorkflowUseCase.execute(command);
        
        // Return the entity for backward compatibility
        return getExecution(result.getExecutionId());
    }
    
    /**
     * Execute workflow using use case pattern
     */
    public WorkflowExecutionResult executeWorkflowWithUseCase(Long workflowId, Map<String, Object> variables, Long userId) {
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(workflowId, variables, userId);
        return executeWorkflowUseCase.execute(command);
    }

    /**
     * Obtener ejecución por ID
     */
    public WorkflowExecution getExecution(Long executionId) {
        return executionRepository.findById(executionId)
                .orElseThrow(() -> new com.javaflow.domain.exception.WorkflowExecutionNotFoundException(executionId));
    }

    /**
     * Obtener ejecución por Process Instance ID
     */
    public WorkflowExecution getExecutionByProcessInstanceId(String processInstanceId) {
        return executionRepository.findByProcessInstanceId(processInstanceId)
                .orElseThrow(() -> new com.javaflow.domain.exception.WorkflowExecutionNotFoundException(processInstanceId, true));
    }

    /**
     * Listar ejecuciones de un workflow
     */
    public List<WorkflowExecution> getExecutionsByWorkflow(Long workflowId) {
        return executionRepository.findByWorkflowId(workflowId);
    }

    /**
     * Listar ejecuciones recientes
     */
    public List<WorkflowExecution> getRecentExecutions(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return executionRepository.findRecentExecutions(since);
    }

    /**
     * Cancelar una ejecución
     */
    @Transactional
    public void cancelExecution(Long executionId) {
        log.info("Cancelling execution: {}", executionId);
        
        WorkflowExecution execution = getExecution(executionId);
        
        // Cancelar en Flowable
        runtimeService.deleteProcessInstance(
                execution.getProcessInstanceId(),
                "Cancelled by user"
        );
        
        // Actualizar estado
        execution.setStatus(WorkflowExecution.ExecutionStatus.CANCELLED);
        execution.setEndedAt(LocalDateTime.now());
        executionRepository.save(execution);
    }

    /**
     * Actualizar estado de ejecución (llamado por listeners de Flowable)
     */
    @Transactional
    public void updateExecutionStatus(String processInstanceId, 
                                     WorkflowExecution.ExecutionStatus status,
                                     String errorMessage) {
        WorkflowExecution execution = getExecutionByProcessInstanceId(processInstanceId);
        execution.setStatus(status);
        
        if (status == WorkflowExecution.ExecutionStatus.COMPLETED ||
            status == WorkflowExecution.ExecutionStatus.FAILED ||
            status == WorkflowExecution.ExecutionStatus.CANCELLED) {
            execution.setEndedAt(LocalDateTime.now());
        }
        
        if (errorMessage != null) {
            execution.setErrorMessage(errorMessage);
        }
        
        executionRepository.save(execution);
    }

    /**
     * Eliminar workflow
     */
    @Transactional
    @CacheEvict(value = "workflows", key = "#id")
    public void deleteWorkflow(Long id) {
        log.info("Deleting workflow: {}", id);
        workflowRepository.deleteById(id);
    }
}
