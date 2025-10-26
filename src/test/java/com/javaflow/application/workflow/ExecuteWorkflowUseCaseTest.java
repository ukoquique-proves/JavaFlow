package com.javaflow.application.workflow;

import com.javaflow.application.workflow.command.ExecuteWorkflowCommand;
import com.javaflow.application.workflow.result.WorkflowExecutionResult;
import com.javaflow.model.User;
import com.javaflow.model.Workflow;
import com.javaflow.model.WorkflowExecution;
import com.javaflow.repository.UserRepository;
import com.javaflow.repository.WorkflowExecutionRepository;
import com.javaflow.repository.WorkflowRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteWorkflowUseCaseTest {

    @Mock
    private WorkflowRepository workflowRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private WorkflowExecutionRepository executionRepository;
    
    @Mock
    private RuntimeService runtimeService;
    
    @InjectMocks
    private ExecuteWorkflowUseCase executeWorkflowUseCase;

    private Workflow activeWorkflow;
    private User testUser;
    private ProcessInstance processInstance;
    private WorkflowExecution execution;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.UserRole.USER)
                .build();

        activeWorkflow = Workflow.builder()
                .id(1L)
                .name("TestWorkflow")
                .description("Test Description")
                .bpmnXml("<bpmn>test</bpmn>")
                .status(Workflow.WorkflowStatus.ACTIVE)
                .createdBy(testUser)
                .build();

        processInstance = mock(ProcessInstance.class);
        // Only stub when needed, not in setUp

        execution = WorkflowExecution.builder()
                .id(1L)
                .workflow(activeWorkflow)
                .processInstanceId("process-123")
                .status(WorkflowExecution.ExecutionStatus.RUNNING)
                .startedBy(testUser)
                .build();
    }

    @Test
    void execute_ShouldSucceed_WhenWorkflowIsActiveAndUserExists() {
        // Given
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(1L, Map.of("key", "value"), 1L);
        
        when(processInstance.getId()).thenReturn("process-123");
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(activeWorkflow));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(runtimeService.startProcessInstanceByKey(eq("TestWorkflow"), anyMap()))
                .thenReturn(processInstance);
        when(executionRepository.save(any(WorkflowExecution.class))).thenReturn(execution);

        // When
        WorkflowExecutionResult result = executeWorkflowUseCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionId()).isEqualTo(1L);
        assertThat(result.getWorkflowId()).isEqualTo(1L);
        assertThat(result.getWorkflowName()).isEqualTo("TestWorkflow");
        assertThat(result.getProcessInstanceId()).isEqualTo("process-123");
        assertThat(result.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.RUNNING);
        assertThat(result.getStartedByUsername()).isEqualTo("testuser");

        verify(workflowRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(runtimeService).startProcessInstanceByKey(eq("TestWorkflow"), anyMap());
        verify(executionRepository).save(any(WorkflowExecution.class));
    }

    @Test
    void execute_ShouldThrowException_WhenWorkflowNotFound() {
        // Given
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(999L, Map.of(), 1L);
        when(workflowRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> executeWorkflowUseCase.execute(command))
                .isInstanceOf(ExecuteWorkflowUseCase.WorkflowNotFoundException.class)
                .hasMessageContaining("Workflow not found with ID: 999");

        verify(workflowRepository).findById(999L);
        verifyNoInteractions(userRepository, runtimeService, executionRepository);
    }

    @Test
    void execute_ShouldThrowException_WhenWorkflowNotActive() {
        // Given
        Workflow inactiveWorkflow = Workflow.builder()
                .id(1L)
                .name("InactiveWorkflow")
                .status(Workflow.WorkflowStatus.DRAFT)
                .build();
        
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(1L, Map.of(), 1L);
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(inactiveWorkflow));

        // When & Then
        assertThatThrownBy(() -> executeWorkflowUseCase.execute(command))
                .isInstanceOf(ExecuteWorkflowUseCase.WorkflowNotActiveException.class)
                .hasMessageContaining("cannot be executed");

        verify(workflowRepository).findById(1L);
        verifyNoInteractions(userRepository, runtimeService, executionRepository);
    }

    @Test
    void execute_ShouldThrowException_WhenUserNotFound() {
        // Given
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(1L, Map.of(), 999L);
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(activeWorkflow));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> executeWorkflowUseCase.execute(command))
                .isInstanceOf(ExecuteWorkflowUseCase.UserNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(workflowRepository).findById(1L);
        verify(userRepository).findById(999L);
        verifyNoInteractions(runtimeService, executionRepository);
    }

    @Test
    void execute_ShouldSucceed_WhenUserIdIsNull() {
        // Given
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(1L, Map.of(), null);
        
        when(processInstance.getId()).thenReturn("process-123");
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(activeWorkflow));
        when(runtimeService.startProcessInstanceByKey(eq("TestWorkflow"), anyMap()))
                .thenReturn(processInstance);
        
        WorkflowExecution systemExecution = WorkflowExecution.builder()
                .id(1L)
                .workflow(activeWorkflow)
                .processInstanceId("process-123")
                .status(WorkflowExecution.ExecutionStatus.RUNNING)
                .startedBy(null)
                .build();
        
        when(executionRepository.save(any(WorkflowExecution.class))).thenReturn(systemExecution);

        // When
        WorkflowExecutionResult result = executeWorkflowUseCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartedByUsername()).isEqualTo("System");

        verify(workflowRepository).findById(1L);
        verifyNoInteractions(userRepository);
        verify(runtimeService).startProcessInstanceByKey(eq("TestWorkflow"), anyMap());
        verify(executionRepository).save(any(WorkflowExecution.class));
    }

    @Test
    void execute_ShouldThrowException_WhenFlowableEngineThrowsException() {
        // Given
        ExecuteWorkflowCommand command = ExecuteWorkflowCommand.of(1L, Map.of(), 1L);
        
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(activeWorkflow));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(runtimeService.startProcessInstanceByKey(eq("TestWorkflow"), anyMap()))
                .thenThrow(new RuntimeException("Flowable engine error"));

        // When & Then
        assertThatThrownBy(() -> executeWorkflowUseCase.execute(command))
                .isInstanceOf(ExecuteWorkflowUseCase.WorkflowExecutionException.class)
                .hasMessageContaining("Failed to start workflow execution")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(workflowRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(runtimeService).startProcessInstanceByKey(eq("TestWorkflow"), anyMap());
        verifyNoInteractions(executionRepository);
    }
}