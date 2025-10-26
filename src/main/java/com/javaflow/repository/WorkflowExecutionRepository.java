package com.javaflow.repository;

import com.javaflow.model.WorkflowExecution;
import com.javaflow.model.WorkflowExecution.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
    
    Optional<WorkflowExecution> findByProcessInstanceId(String processInstanceId);
    
    List<WorkflowExecution> findByWorkflowId(Long workflowId);
    
    List<WorkflowExecution> findByStatus(ExecutionStatus status);
    
    List<WorkflowExecution> findByWorkflowIdAndStatus(Long workflowId, ExecutionStatus status);
    
    @Query("SELECT e FROM WorkflowExecution e WHERE e.startedAt >= :since ORDER BY e.startedAt DESC")
    List<WorkflowExecution> findRecentExecutions(LocalDateTime since);
    
    long countByStatus(ExecutionStatus status);
}
