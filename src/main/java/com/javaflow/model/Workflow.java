package com.javaflow.model;

import com.javaflow.domain.events.WorkflowActivatedEvent;
import com.javaflow.domain.exception.WorkflowAlreadyActiveException;
import com.javaflow.domain.exception.WorkflowCannotBeActivatedException;
import com.javaflow.domain.exception.WorkflowNotActiveException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad de Workflow (Flujo de trabajo)
 */
@Entity
@Table(name = "workflows", indexes = {
    @Index(name = "idx_workflow_status", columnList = "status"),
    @Index(name = "idx_workflow_created_by", columnList = "created_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "bpmn_xml", columnDefinition = "TEXT", nullable = false)
    private String bpmnXml;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<WorkflowExecution> executions = new HashSet<>();

    @Transient
    private final transient List<Object> domainEvents = new ArrayList<>();

    public enum WorkflowStatus {
        DRAFT,
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }

    // ========== DOMAIN BUSINESS METHODS ==========

    /**
     * Activates the workflow if it meets the business rules
     * 
     * @throws WorkflowAlreadyActiveException if workflow is already active
     * @throws WorkflowCannotBeActivatedException if workflow cannot be activated
     */
    public void activate() {
        if (isActive()) {
            throw new WorkflowAlreadyActiveException(
                String.format("Workflow '%s' is already active", this.name)
            );
        }
        
        if (!canBeActivated()) {
            throw new WorkflowCannotBeActivatedException(
                String.format("Workflow '%s' cannot be activated: %s", 
                    this.name, getActivationBlockingReason())
            );
        }
        
        this.status = WorkflowStatus.ACTIVE;
        this.domainEvents.add(new WorkflowActivatedEvent(this.id, this.name, LocalDateTime.now()));
    }

    /**
     * Deactivates the workflow
     */
    public void deactivate() {
        if (!isActive()) {
            throw new WorkflowCannotBeActivatedException(
                String.format("Workflow '%s' is not active, current status: %s", 
                    this.name, this.status)
            );
        }
        
        this.status = WorkflowStatus.INACTIVE;
    }

    /**
     * Archives the workflow (cannot be reactivated)
     */
    public void archive() {
        if (isActive()) {
            throw new WorkflowCannotBeActivatedException(
                String.format("Cannot archive active workflow '%s'. Deactivate it first.", this.name)
            );
        }
        
        this.status = WorkflowStatus.ARCHIVED;
    }

    /**
     * Creates a new workflow execution
     * 
     * @param variables Process variables
     * @param startedBy User who started the execution
     * @return New WorkflowExecution instance
     * @throws WorkflowNotActiveException if workflow is not active
     */
    public WorkflowExecution createExecution(Map<String, Object> variables, User startedBy) {
        if (!isActive()) {
            throw new WorkflowNotActiveException(
                String.format("Cannot execute workflow '%s'. Current status: %s", 
                    this.name, this.status)
            );
        }
        
        return WorkflowExecution.builder()
                .workflow(this)
                .status(WorkflowExecution.ExecutionStatus.RUNNING)
                .startedBy(startedBy)
                .build();
    }

    // ========== DOMAIN VALIDATION METHODS ==========

    /**
     * Checks if the workflow can be activated
     */
    public boolean canBeActivated() {
        return this.status == WorkflowStatus.DRAFT && 
               hasValidBpmnDefinition() &&
               hasValidName();
    }

    /**
     * Checks if the workflow is currently active
     */
    public boolean isActive() {
        return this.status == WorkflowStatus.ACTIVE;
    }

    /**
     * Checks if the workflow is archived
     */
    public boolean isArchived() {
        return this.status == WorkflowStatus.ARCHIVED;
    }

    /**
     * Checks if the workflow can be executed
     */
    public boolean canBeExecuted() {
        return isActive() && hasValidBpmnDefinition();
    }

    /**
     * Validates if the BPMN definition is valid
     */
    public boolean hasValidBpmnDefinition() {
        return this.bpmnXml != null && 
               !this.bpmnXml.trim().isEmpty() &&
               (this.bpmnXml.contains("<bpmn") || this.bpmnXml.contains("<definitions"));
    }

    /**
     * Validates if the workflow name is valid
     */
    public boolean hasValidName() {
        return this.name != null && 
               !this.name.trim().isEmpty() &&
               this.name.length() >= 3 &&
               this.name.length() <= 100;
    }

    /**
     * Gets the total number of executions
     */
    public int getExecutionCount() {
        return this.executions != null ? this.executions.size() : 0;
    }

    /**
     * Gets the number of successful executions
     */
    public long getSuccessfulExecutionCount() {
        if (this.executions == null) return 0;
        
        return this.executions.stream()
                .filter(execution -> execution.getStatus() == WorkflowExecution.ExecutionStatus.COMPLETED)
                .count();
    }

    /**
     * Gets the number of failed executions
     */
    public long getFailedExecutionCount() {
        if (this.executions == null) return 0;
        
        return this.executions.stream()
                .filter(execution -> execution.getStatus() == WorkflowExecution.ExecutionStatus.FAILED)
                .count();
    }

    /**
     * Calculates the success rate of executions
     */
    public double getSuccessRate() {
        int totalExecutions = getExecutionCount();
        if (totalExecutions == 0) return 0.0;
        
        return (double) getSuccessfulExecutionCount() / totalExecutions * 100.0;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Gets the reason why the workflow cannot be activated
     */
    // ========== DOMAIN EVENT METHODS ==========

    @DomainEvents
    public Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== PRIVATE HELPER METHODS ==========

    private String getActivationBlockingReason() {
        if (this.status == WorkflowStatus.ARCHIVED) {
            return "workflow is archived";
        }
        if (this.status == WorkflowStatus.ACTIVE) {
            return "workflow is already active";
        }
        if (!hasValidBpmnDefinition()) {
            return "BPMN definition is missing or invalid";
        }
        if (!hasValidName()) {
            return "workflow name is invalid";
        }
        return "unknown reason";
    }
}
