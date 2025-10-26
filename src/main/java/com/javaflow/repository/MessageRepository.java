package com.javaflow.repository;

import com.javaflow.model.Message;
import com.javaflow.model.Message.MessageDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByBotId(Long botId);
    
    List<Message> findByChatId(String chatId);
    
    List<Message> findByChatIdOrderByCreatedAtDesc(String chatId);
    
    List<Message> findByDirection(MessageDirection direction);
    
    List<Message> findByWorkflowExecutionId(Long executionId);
    
    @Query("SELECT m FROM Message m WHERE m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(LocalDateTime since);
    
    long countByDirection(MessageDirection direction);
}
