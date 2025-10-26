package com.javaflow.model;

import com.javaflow.domain.events.WorkflowExecutedEvent;
import com.javaflow.domain.exception.WorkflowExecutionException;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad de Ejecución de Workflow
 */
@Entity
@Table(name = "workflow_executions", indexes = {
    @Index(name = "idx_execution_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_execution_status", columnList = "status"),
    @Index(name = "idx_execution_process_instance_id", columnList = "process_instance_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "process_instance_id", unique = true)
    private String processInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private User startedBy;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String variables;

    @Transient
    private final transient List<Object> domainEvents = new ArrayList<>();

    public enum ExecutionStatus {
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        SUSPENDED
    }

    // ========== DOMAIN BUSINESS METHODS ==========

    /**
     * Completes the execution successfully
     */
    public void complete() {
        if (!isRunning()) {
            throw new WorkflowExecutionException(
                String.format("Cannot complete execution %d. Current status: %s", this.id, this.status)
            );
        }
        
        this.status = ExecutionStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Marks the execution as failed with an error message
     */
    public void fail(String errorMessage) {
        if (isFinished()) {
            throw new WorkflowExecutionException(
                String.format("Cannot fail execution %d. Current status: %s", this.id, this.status)
            );
        }
        
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Cancels the execution
     */
    public void cancel() {
        if (isFinished()) {
            throw new WorkflowExecutionException(
                String.format("Cannot cancel execution %d. Current status: %s", this.id, this.status)
            );
        }
        
        this.status = ExecutionStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Suspends the execution
     */
    public void suspend() {
        if (!isRunning()) {
            throw new WorkflowExecutionException(
                String.format("Cannot suspend execution %d. Current status: %s", this.id, this.status)
            );
        }
        
        this.status = ExecutionStatus.SUSPENDED;
    }

    /**
     * Resumes a suspended execution
     */
    public void resume() {
        if (this.status != ExecutionStatus.SUSPENDED) {
            throw new WorkflowExecutionException(
                String.format("Cannot resume execution %d. Current status: %s", this.id, this.status)
            );
        }
        
        this.status = ExecutionStatus.RUNNING;
    }

    // ========== DOMAIN QUERY METHODS ==========

    /**
     * Checks if the execution is currently running
     */
    public boolean isRunning() {
        return this.status == ExecutionStatus.RUNNING;
    }

    /**
     * Checks if the execution is finished (completed, failed, or cancelled)
     */
    public boolean isFinished() {
        return this.status == ExecutionStatus.COMPLETED ||
               this.status == ExecutionStatus.FAILED ||
               this.status == ExecutionStatus.CANCELLED;
    }

    /**
     * Checks if the execution completed successfully
     */
    public boolean isSuccessful() {
        return this.status == ExecutionStatus.COMPLETED;
    }

    /**
     * Checks if the execution failed
     */
    public boolean isFailed() {
        return this.status == ExecutionStatus.FAILED;
    }

    /**
     * Checks if the execution was cancelled
     */
    public boolean isCancelled() {
        return this.status == ExecutionStatus.CANCELLED;
    }

    /**
     * Checks if the execution is suspended
     */
    public boolean isSuspended() {
        return this.status == ExecutionStatus.SUSPENDED;
    }

    /**
     * Gets the execution duration
     */
    public Duration getDuration() {
        LocalDateTime endTime = this.endedAt != null ? this.endedAt : LocalDateTime.now();
        return Duration.between(this.startedAt, endTime);
    }

    /**
     * Gets the execution duration in milliseconds
     */
    public long getDurationMillis() {
        return getDuration().toMillis();
    }

    /**
     * Gets the execution duration in seconds
     */
    public long getDurationSeconds() {
        return getDuration().getSeconds();
    }

    /**
     * Checks if the execution has been running longer than the specified duration
     */
    public boolean isRunningLongerThan(Duration maxDuration) {
        if (!isRunning()) return false;
        return getDuration().compareTo(maxDuration) > 0;
    }

    /**
     * Gets a human-readable status description
     */
    public String getStatusDescription() {
        return switch (this.status) {
            case RUNNING -> "En ejecución";
            case COMPLETED -> "Completado exitosamente";
            case FAILED -> "Falló: " + (errorMessage != null ? errorMessage : "Error desconocido");
            case CANCELLED -> "Cancelado";
            case SUSPENDED -> "Suspendido";
        };
    }

    /**
     * Creates a new execution for the given workflow
     */
    public static WorkflowExecution create(Workflow workflow, User startedBy) {
        WorkflowExecution execution = WorkflowExecution.builder()
                .workflow(workflow)
                .status(ExecutionStatus.RUNNING)
                .startedBy(startedBy)
                .build();

        // The event will be published after the entity is saved and has an ID.
        execution.domainEvents.add(new WorkflowExecutedEvent(
                null, // ID will be available after save
                workflow.getId(),
                ExecutionStatus.RUNNING,
                LocalDateTime.now()
        ));

        return execution;
    }

    // ========== DOMAIN EVENT METHODS ==========

    @DomainEvents
    public Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
