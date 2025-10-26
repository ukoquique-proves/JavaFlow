package com.javaflow.ui.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaflow.application.dto.workflow.CreateWorkflowRequest;
import com.javaflow.application.dto.workflow.ExecuteWorkflowRequest;
import com.javaflow.application.workflow.ActivateWorkflowUseCase;
import com.javaflow.application.workflow.CreateWorkflowUseCase;
import com.javaflow.application.workflow.ExecuteWorkflowUseCase;
import com.javaflow.application.workflow.result.WorkflowExecutionResult;
import com.javaflow.application.workflow.result.WorkflowResult;
import com.javaflow.model.Workflow;
import com.javaflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(WorkflowRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateWorkflowUseCase createWorkflowUseCase;

    @MockBean
    private ActivateWorkflowUseCase activateWorkflowUseCase;

    @MockBean
    private ExecuteWorkflowUseCase executeWorkflowUseCase;

    @MockBean
    private WorkflowService workflowService;

    @Test
    void createWorkflow_ShouldReturnCreatedWorkflow_WhenValidRequest() throws Exception {
        // Given
        CreateWorkflowRequest request = CreateWorkflowRequest.of(
            "Test Workflow",
            "Test Description",
            "<definitions><process id='test'><startEvent/><endEvent/></process></definitions>"
        );

        WorkflowResult result = WorkflowResult.builder()
                .id(1L)
                .name("Test Workflow")
                .description("Test Description")
                .status(Workflow.WorkflowStatus.DRAFT)
                .version(1)
                .createdAt(LocalDateTime.now())
                .createdByUsername("testuser")
                .build();

        when(createWorkflowUseCase.execute(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Workflow"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createWorkflow_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        CreateWorkflowRequest request = CreateWorkflowRequest.of(
            "", // Invalid name
            "Test Description",
            "<definitions><process id='test'><startEvent/><endEvent/></process></definitions>"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void getAllWorkflows_ShouldReturnWorkflowList() throws Exception {
        // Given
        Workflow workflow = Workflow.builder()
                .id(1L)
                .name("Test Workflow")
                .description("Test Description")
                .status(Workflow.WorkflowStatus.ACTIVE)
                .version(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(workflowService.getAllWorkflows()).thenReturn(List.of(workflow));

        // When & Then
        mockMvc.perform(get("/api/v1/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Workflow"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void activateWorkflow_ShouldReturnActivatedWorkflow() throws Exception {
        // Given
        WorkflowResult result = WorkflowResult.builder()
                .id(1L)
                .name("Test Workflow")
                .status(Workflow.WorkflowStatus.ACTIVE)
                .version(1)
                .createdAt(LocalDateTime.now())
                .createdByUsername("testuser")
                .build();

        when(activateWorkflowUseCase.execute(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/workflows/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void executeWorkflow_ShouldReturnExecution_WhenValidRequest() throws Exception {
        // Given
        ExecuteWorkflowRequest request = ExecuteWorkflowRequest.of(
            1L,
            Map.of("key", "value"),
            1L
        );

        WorkflowExecutionResult result = WorkflowExecutionResult.builder()
                .executionId(1L)
                .workflowId(1L)
                .workflowName("Test Workflow")
                .processInstanceId("process-123")
                .status(com.javaflow.model.WorkflowExecution.ExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .startedByUsername("testuser")
                .build();

        when(executeWorkflowUseCase.execute(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/workflows/1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workflowId").value(1))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }
}