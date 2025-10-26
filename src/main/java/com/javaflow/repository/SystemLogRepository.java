package com.javaflow.repository;

import com.javaflow.model.SystemLog;
import com.javaflow.model.SystemLog.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    
    Page<SystemLog> findByLevel(LogLevel level, Pageable pageable);
    
    Page<SystemLog> findByWorkflowExecutionId(Long executionId, Pageable pageable);
    
    @Query("SELECT l FROM SystemLog l WHERE l.createdAt >= :since ORDER BY l.createdAt DESC")
    List<SystemLog> findRecentLogs(LocalDateTime since, Pageable pageable);
    
    long countByLevel(LogLevel level);
}
