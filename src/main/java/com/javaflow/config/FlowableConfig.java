package com.javaflow.config;

import org.flowable.engine.*;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Configuración del Motor Flowable BPMN
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        // Configuración personalizada del motor
        engineConfiguration.setActivityFontName("Arial");
        engineConfiguration.setLabelFontName("Arial");
        engineConfiguration.setAnnotationFontName("Arial");
        
        // Async executor configuration
        engineConfiguration.setAsyncExecutorActivate(true);
        engineConfiguration.setAsyncExecutorCorePoolSize(2);
        engineConfiguration.setAsyncExecutorMaxPoolSize(10);
        engineConfiguration.setAsyncExecutorThreadPoolQueueSize(100);
        
        // History level
        engineConfiguration.setHistoryLevel(HistoryLevel.FULL);
        
        // Database schema update
        engineConfiguration.setDatabaseSchemaUpdate("true");
    }

    /**
     * RuntimeService: Para iniciar y gestionar instancias de procesos
     */
    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    /**
     * TaskService: Para gestionar tareas de usuario
     */
    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    /**
     * RepositoryService: Para gestionar definiciones de procesos
     */
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    /**
     * HistoryService: Para consultar datos históricos
     */
    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    /**
     * ManagementService: Para operaciones de gestión y mantenimiento
     */
    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }
}
