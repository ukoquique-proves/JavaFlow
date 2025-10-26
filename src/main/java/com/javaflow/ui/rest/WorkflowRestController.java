package com.javaflow.ui.rest;

import com.javaflow.application.dto.common.PageResponse;
import com.javaflow.application.dto.workflow.*;
import com.javaflow.application.workflow.ActivateWorkflowUseCase;
import com.javaflow.application.workflow.CreateWorkflowUseCase;
import com.javaflow.application.workflow.ExecuteWorkflowUseCase;
import com.javaflow.application.workflow.command.ActivateWorkflowCommand;
import com.javaflow.application.workflow.command.CreateWorkflowCommand;
import com.javaflow.application.workflow.command.ExecuteWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowExecutionResult;
import com.javaflow.application.workflow.result.WorkflowResult;
import com.javaflow.model.Workflow;
import com.javaflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for workflow operations using DTOs
 * 
 * This controller demonstrates proper API boundaries with:
 * - Input validation through DTOs
 * - Domain protection via DTOs
 * - Consistent response format
 * - Proper error handling
 */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Slf4j
public class WorkflowRestController {

    private final CreateWorkflowUseCase createWorkflowUseCase;
    private final ActivateWorkflowUseCase activateWorkflowUseCase;
    private final ExecuteWorkflowUseCase executeWorkflowUseCase;
    private final WorkflowService workflowService; // For queries

    /**
     * Create a new workflow
     */
    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("Creating workflow: {}", request.getName());
        
        CreateWorkflowCommand command = CreateWorkflowCommand.of(
            request.getName(),
            request.getDescription(),
            request.getBpmnXml(),
            userId != null ? userId : 1L // Default user for demo
        );
        
        WorkflowResult result = createWorkflowUseCase.execute(command);
        WorkflowResponse response = WorkflowResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .description(result.getDescription())
                .status(result.getStatus().toString())
                .version(result.getVersion())
                .createdAt(result.getCreatedAt())
                .createdBy(result.getCreatedByUsername())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all workflows
     */
    @GetMapping
    public ResponseEntity<PageResponse<WorkflowResponse>> getAllWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting workflows - page: {}, size: {}", page, size);
        
        List<Workflow> workflows = workflowService.getAllWorkflows();
        List<WorkflowResponse> responses = workflows.stream()
                .map(WorkflowResponse::from)
                .toList();
        
        PageResponse<WorkflowResponse> pageResponse = PageResponse.single(responses);
        return ResponseEntity.ok(pageResponse);
    }

    /**
     * Get workflow by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable Long id) {
        log.debug("Getting workflow: {}", id);
        
        Workflow workflow = workflowService.getWorkflow(id);
        WorkflowResponse response = WorkflowResponse.from(workflow);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a workflow
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<WorkflowResponse> activateWorkflow(@PathVariable Long id) {
        log.info("Activating workflow: {}", id);
        
        ActivateWorkflowCommand command = ActivateWorkflowCommand.of(id);
        WorkflowResult result = activateWorkflowUseCase.execute(command);
        
        WorkflowResponse response = WorkflowResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .description(result.getDescription())
                .status(result.getStatus().toString())
                .version(result.getVersion())
                .createdAt(result.getCreatedAt())
                .createdBy(result.getCreatedByUsername())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Execute a workflow
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<WorkflowExecutionResponse> executeWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody ExecuteWorkflowRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("Executing workflow: {}", id);
        
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(
            id,
            request.getVariables(),
            userId != null ? userId : request.getStartedByUserId()
        );
        
        WorkflowExecutionResult result = executeWorkflowUseCase.execute(command);
        
        WorkflowExecutionResponse response = WorkflowExecutionResponse.builder()
                .id(result.getExecutionId())
                .workflowId(result.getWorkflowId())
                .workflowName(result.getWorkflowName())
                .processInstanceId(result.getProcessInstanceId())
                .status(result.getStatus().toString())
                .startedAt(result.getStartedAt())
                .startedBy(result.getStartedByUsername())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get workflow executions
     */
    @GetMapping("/{id}/executions")
    public ResponseEntity<PageResponse<WorkflowExecutionResponse>> getWorkflowExecutions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting executions for workflow: {}", id);
        
        List<com.javaflow.model.WorkflowExecution> executions = workflowService.getExecutionsByWorkflow(id);
        List<WorkflowExecutionResponse> responses = executions.stream()
                .map(WorkflowExecutionResponse::from)
                .toList();
        
        PageResponse<WorkflowExecutionResponse> pageResponse = PageResponse.single(responses);
        return ResponseEntity.ok(pageResponse);
    }
}