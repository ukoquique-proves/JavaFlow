package com.javaflow.repository;

import com.javaflow.model.BotConfiguration;
import com.javaflow.model.BotConfiguration.BotStatus;
import com.javaflow.model.BotConfiguration.BotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotConfigurationRepository extends JpaRepository<BotConfiguration, Long> {
    
    List<BotConfiguration> findByType(BotType type);
    
    List<BotConfiguration> findByStatus(BotStatus status);
    
    List<BotConfiguration> findByTypeAndStatus(BotType type, BotStatus status);
}
