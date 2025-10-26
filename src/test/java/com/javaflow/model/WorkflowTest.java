package com.javaflow.model;

import com.javaflow.domain.exception.WorkflowAlreadyActiveException;
import com.javaflow.domain.exception.WorkflowCannotBeActivatedException;
import com.javaflow.domain.exception.WorkflowNotActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class WorkflowTest {

    private Workflow draftWorkflow;
    private Workflow activeWorkflow;
    private Workflow archivedWorkflow;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.UserRole.USER)
                .build();

        draftWorkflow = Workflow.builder()
                .id(1L)
                .name("Test Workflow")
                .description("Test Description")
                .bpmnXml("<definitions><process id='test'><startEvent/><endEvent/></process></definitions>")
                .status(Workflow.WorkflowStatus.DRAFT)
                .createdBy(testUser)
                .build();

        activeWorkflow = Workflow.builder()
                .id(2L)
                .name("Active Workflow")
                .bpmnXml("<definitions><process id='active'><startEvent/><endEvent/></process></definitions>")
                .status(Workflow.WorkflowStatus.ACTIVE)
                .createdBy(testUser)
                .build();

        archivedWorkflow = Workflow.builder()
                .id(3L)
                .name("Archived Workflow")
                .bpmnXml("<definitions><process id='archived'><startEvent/><endEvent/></process></definitions>")
                .status(Workflow.WorkflowStatus.ARCHIVED)
                .createdBy(testUser)
                .build();
    }

    @Test
    void activate_ShouldSucceed_WhenWorkflowIsDraftAndValid() {
        // When
        draftWorkflow.activate();

        // Then
        assertThat(draftWorkflow.getStatus()).isEqualTo(Workflow.WorkflowStatus.ACTIVE);
        assertThat(draftWorkflow.isActive()).isTrue();
    }

    @Test
    void activate_ShouldThrowException_WhenWorkflowIsAlreadyActive() {
        // When & Then
        assertThatThrownBy(() -> activeWorkflow.activate())
                .isInstanceOf(WorkflowAlreadyActiveException.class)
                .hasMessageContaining("is already active");
    }

    @Test
    void activate_ShouldThrowException_WhenWorkflowIsArchived() {
        // When & Then
        assertThatThrownBy(() -> archivedWorkflow.activate())
                .isInstanceOf(WorkflowCannotBeActivatedException.class)
                .hasMessageContaining("cannot be activated");
    }

    @Test
    void activate_ShouldThrowException_WhenBpmnIsInvalid() {
        // Given
        draftWorkflow.setBpmnXml("");

        // When & Then
        assertThatThrownBy(() -> draftWorkflow.activate())
                .isInstanceOf(WorkflowCannotBeActivatedException.class)
                .hasMessageContaining("cannot be activated");
    }

    @Test
    void deactivate_ShouldSucceed_WhenWorkflowIsActive() {
        // When
        activeWorkflow.deactivate();

        // Then
        assertThat(activeWorkflow.getStatus()).isEqualTo(Workflow.WorkflowStatus.INACTIVE);
        assertThat(activeWorkflow.isActive()).isFalse();
    }

    @Test
    void deactivate_ShouldThrowException_WhenWorkflowIsNotActive() {
        // When & Then
        assertThatThrownBy(() -> draftWorkflow.deactivate())
                .isInstanceOf(WorkflowCannotBeActivatedException.class)
                .hasMessageContaining("is not active");
    }

    @Test
    void archive_ShouldSucceed_WhenWorkflowIsNotActive() {
        // When
        draftWorkflow.archive();

        // Then
        assertThat(draftWorkflow.getStatus()).isEqualTo(Workflow.WorkflowStatus.ARCHIVED);
        assertThat(draftWorkflow.isArchived()).isTrue();
    }

    @Test
    void archive_ShouldThrowException_WhenWorkflowIsActive() {
        // When & Then
        assertThatThrownBy(() -> activeWorkflow.archive())
                .isInstanceOf(WorkflowCannotBeActivatedException.class)
                .hasMessageContaining("Cannot archive active workflow");
    }

    @Test
    void createExecution_ShouldSucceed_WhenWorkflowIsActive() {
        // When
        WorkflowExecution execution = activeWorkflow.createExecution(Map.of("key", "value"), testUser);

        // Then
        assertThat(execution).isNotNull();
        assertThat(execution.getWorkflow()).isEqualTo(activeWorkflow);
        assertThat(execution.getStartedBy()).isEqualTo(testUser);
        assertThat(execution.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.RUNNING);
    }

    @Test
    void createExecution_ShouldThrowException_WhenWorkflowIsNotActive() {
        // When & Then
        assertThatThrownBy(() -> draftWorkflow.createExecution(Map.of(), testUser))
                .isInstanceOf(WorkflowNotActiveException.class)
                .hasMessageContaining("Cannot execute workflow");
    }

    @Test
    void canBeActivated_ShouldReturnTrue_WhenWorkflowIsDraftAndValid() {
        // When & Then
        assertThat(draftWorkflow.canBeActivated()).isTrue();
    }

    @Test
    void canBeActivated_ShouldReturnFalse_WhenWorkflowIsActive() {
        // When & Then
        assertThat(activeWorkflow.canBeActivated()).isFalse();
    }

    @Test
    void canBeActivated_ShouldReturnFalse_WhenBpmnIsInvalid() {
        // Given
        draftWorkflow.setBpmnXml("");

        // When & Then
        assertThat(draftWorkflow.canBeActivated()).isFalse();
    }

    @Test
    void canBeActivated_ShouldReturnFalse_WhenNameIsInvalid() {
        // Given
        draftWorkflow.setName("ab"); // Too short

        // When & Then
        assertThat(draftWorkflow.canBeActivated()).isFalse();
    }

    @Test
    void canBeExecuted_ShouldReturnTrue_WhenWorkflowIsActiveAndValid() {
        // When & Then
        assertThat(activeWorkflow.canBeExecuted()).isTrue();
    }

    @Test
    void canBeExecuted_ShouldReturnFalse_WhenWorkflowIsNotActive() {
        // When & Then
        assertThat(draftWorkflow.canBeExecuted()).isFalse();
    }

    @Test
    void hasValidBpmnDefinition_ShouldReturnTrue_WhenBpmnIsValid() {
        // When & Then
        assertThat(draftWorkflow.hasValidBpmnDefinition()).isTrue();
    }

    @Test
    void hasValidBpmnDefinition_ShouldReturnFalse_WhenBpmnIsEmpty() {
        // Given
        draftWorkflow.setBpmnXml("");

        // When & Then
        assertThat(draftWorkflow.hasValidBpmnDefinition()).isFalse();
    }

    @Test
    void hasValidBpmnDefinition_ShouldReturnFalse_WhenBpmnIsNull() {
        // Given
        draftWorkflow.setBpmnXml(null);

        // When & Then
        assertThat(draftWorkflow.hasValidBpmnDefinition()).isFalse();
    }

    @Test
    void hasValidName_ShouldReturnTrue_WhenNameIsValid() {
        // When & Then
        assertThat(draftWorkflow.hasValidName()).isTrue();
    }

    @Test
    void hasValidName_ShouldReturnFalse_WhenNameIsTooShort() {
        // Given
        draftWorkflow.setName("ab");

        // When & Then
        assertThat(draftWorkflow.hasValidName()).isFalse();
    }

    @Test
    void hasValidName_ShouldReturnFalse_WhenNameIsTooLong() {
        // Given
        draftWorkflow.setName("a".repeat(101));

        // When & Then
        assertThat(draftWorkflow.hasValidName()).isFalse();
    }

    @Test
    void hasValidName_ShouldReturnFalse_WhenNameIsNull() {
        // Given
        draftWorkflow.setName(null);

        // When & Then
        assertThat(draftWorkflow.hasValidName()).isFalse();
    }

    @Test
    void getExecutionCount_ShouldReturnZero_WhenNoExecutions() {
        // When & Then
        assertThat(draftWorkflow.getExecutionCount()).isEqualTo(0);
    }

    @Test
    void getSuccessRate_ShouldReturnZero_WhenNoExecutions() {
        // When & Then
        assertThat(draftWorkflow.getSuccessRate()).isEqualTo(0.0);
    }
}