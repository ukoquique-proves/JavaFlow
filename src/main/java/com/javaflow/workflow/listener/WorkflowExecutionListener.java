package com.javaflow.workflow.listener;

import com.javaflow.model.WorkflowExecution;
import com.javaflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de ejecución de workflows
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionListener implements ExecutionListener {

    private final WorkflowService workflowService;

    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        String processInstanceId = execution.getProcessInstanceId();
        
        log.info("Workflow event: {} for process: {}", eventName, processInstanceId);
        
        try {
            switch (eventName) {
                case "start":
                    handleStart(execution);
                    break;
                case "end":
                    handleEnd(execution);
                    break;
                default:
                    log.debug("Unhandled event: {}", eventName);
            }
        } catch (Exception e) {
            log.error("Error handling workflow event", e);
        }
    }

    private void handleStart(DelegateExecution execution) {
        log.info("Workflow started: {}", execution.getProcessInstanceId());
        // El estado RUNNING ya se setea en WorkflowService.executeWorkflow()
    }

    private void handleEnd(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        log.info("Workflow completed: {}", processInstanceId);
        
        // Actualizar estado a COMPLETED
        workflowService.updateExecutionStatus(
            processInstanceId,
            WorkflowExecution.ExecutionStatus.COMPLETED,
            null
        );
    }
}
