package com.javaflow.workflow.task;

import com.javaflow.model.SystemLog;
import com.javaflow.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Tarea personalizada de Flowable para registrar logs
 * 
 * Variables esperadas:
 * - logLevel: Nivel del log (INFO, WARN, ERROR)
 * - logMessage: Mensaje a registrar
 */
@Component("logTask")
@RequiredArgsConstructor
@Slf4j
public class LogTask implements JavaDelegate {

    private final SystemLogRepository logRepository;

    @Override
    public void execute(DelegateExecution execution) {
        String level = (String) execution.getVariable("logLevel");
        String message = (String) execution.getVariable("logMessage");
        
        if (message == null) {
            message = "Workflow step executed: " + execution.getCurrentActivityId();
        }
        
        SystemLog.LogLevel logLevel = SystemLog.LogLevel.INFO;
        if (level != null) {
            try {
                logLevel = SystemLog.LogLevel.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid log level: {}, using INFO", level);
            }
        }
        
        SystemLog systemLog = SystemLog.builder()
                .level(logLevel)
                .logger("WorkflowTask")
                .message(message)
                .build();
        
        logRepository.save(systemLog);
        
        log.info("Log saved: {} - {}", logLevel, message);
    }
}
