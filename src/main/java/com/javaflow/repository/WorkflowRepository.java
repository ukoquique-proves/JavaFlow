package com.javaflow.repository;

import com.javaflow.model.Workflow;
import com.javaflow.model.Workflow.WorkflowStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    // ========== OPTIMIZED QUERIES WITH ENTITY GRAPHS (Prevent N+1) ==========
    
    /**
     * Find all workflows with creator eagerly loaded
     * Prevents N+1 query when accessing createdBy
     */
    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT w FROM Workflow w")
    List<Workflow> findAllWithCreator();
    
    /**
     * Find all workflows with creator and executions eagerly loaded
     * Use for dashboard or detailed views
     */
    @EntityGraph(attributePaths = {"createdBy", "executions"})
    @Query("SELECT DISTINCT w FROM Workflow w")
    List<Workflow> findAllWithExecutions();
    
    /**
     * Find workflow by ID with all relationships loaded
     */
    @EntityGraph(attributePaths = {"createdBy", "executions"})
    Optional<Workflow> findWithDetailsById(Long id);
    
    /**
     * Find workflows by status with creator loaded
     */
    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT w FROM Workflow w WHERE w.status = :status")
    List<Workflow> findByStatusWithCreator(@Param("status") WorkflowStatus status);
    
    // ========== STANDARD QUERIES (Use for simple lookups) ==========
    
    List<Workflow> findByStatus(WorkflowStatus status);
    
    List<Workflow> findByCreatedById(Long userId);
    
    List<Workflow> findByStatusAndCreatedById(WorkflowStatus status, Long userId);
    
    Optional<Workflow> findByName(String name);
}
