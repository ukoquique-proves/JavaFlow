package com.javaflow.domain.service;

import com.javaflow.model.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain service for complex workflow validation logic
 */
@Component
@Slf4j
public class WorkflowValidationService {

    private static final Pattern BPMN_START_PATTERN = Pattern.compile(
        ".*<(bpmn:|bpmn2:|)definitions.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PROCESS_PATTERN = Pattern.compile(
        ".*<(bpmn:|bpmn2:|)process.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /**
     * Validates BPMN XML content
     */
    public ValidationResult validateBpmn(String bpmnXml) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            errors.add("BPMN XML is required");
            return ValidationResult.invalid(errors, warnings);
        }

        // Check basic XML structure
        if (!isValidXmlStructure(bpmnXml)) {
            errors.add("Invalid XML structure");
        }

        // Check BPMN namespace
        if (!hasBpmnNamespace(bpmnXml)) {
            errors.add("Missing BPMN namespace or definitions element");
        }

        // Check for process definition
        if (!hasProcessDefinition(bpmnXml)) {
            warnings.add("No process definition found in BPMN");
        }

        // Check for start event
        if (!hasStartEvent(bpmnXml)) {
            warnings.add("No start event found in process");
        }

        // Check for end event
        if (!hasEndEvent(bpmnXml)) {
            warnings.add("No end event found in process");
        }

        return errors.isEmpty() ? 
            ValidationResult.valid(warnings) : 
            ValidationResult.invalid(errors, warnings);
    }

    /**
     * Validates workflow business rules
     */
    public ValidationResult validateWorkflowBusinessRules(Workflow workflow) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Name validation
        if (!isValidWorkflowName(workflow.getName())) {
            errors.add("Workflow name must be between 3 and 100 characters and contain only letters, numbers, spaces, and hyphens");
        }

        // Check for duplicate names (this would need repository access in real implementation)
        // For now, we'll add it as a warning
        if (workflow.getName() != null && workflow.getName().toLowerCase().contains("test")) {
            warnings.add("Workflow name contains 'test' - consider using a more descriptive name for production");
        }

        // Version validation
        if (workflow.getVersion() != null && workflow.getVersion() < 1) {
            errors.add("Workflow version must be greater than 0");
        }

        // Status validation
        if (workflow.getStatus() == null) {
            errors.add("Workflow status is required");
        }

        return errors.isEmpty() ? 
            ValidationResult.valid(warnings) : 
            ValidationResult.invalid(errors, warnings);
    }

    /**
     * Validates if a workflow can be safely deleted
     */
    public ValidationResult validateWorkflowDeletion(Workflow workflow) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (workflow.isActive()) {
            errors.add("Cannot delete active workflow. Deactivate it first.");
        }

        if (workflow.getExecutionCount() > 0) {
            warnings.add(String.format("Workflow has %d executions. Deleting will remove execution history.", 
                workflow.getExecutionCount()));
        }

        long runningExecutions = workflow.getExecutions().stream()
            .filter(execution -> execution.isRunning() || execution.isSuspended())
            .count();

        if (runningExecutions > 0) {
            errors.add(String.format("Cannot delete workflow with %d running or suspended executions", runningExecutions));
        }

        return errors.isEmpty() ? 
            ValidationResult.valid(warnings) : 
            ValidationResult.invalid(errors, warnings);
    }

    // ========== PRIVATE VALIDATION METHODS ==========

    private boolean isValidXmlStructure(String xml) {
        try {
            // Basic XML structure check
            return xml.trim().startsWith("<") && 
                   xml.trim().endsWith(">") &&
                   xml.contains("<?xml") || xml.contains("<definitions") || xml.contains("<bpmn");
        } catch (Exception e) {
            log.debug("XML structure validation failed", e);
            return false;
        }
    }

    private boolean hasBpmnNamespace(String bpmnXml) {
        return BPMN_START_PATTERN.matcher(bpmnXml).matches();
    }

    private boolean hasProcessDefinition(String bpmnXml) {
        return PROCESS_PATTERN.matcher(bpmnXml).matches();
    }

    private boolean hasStartEvent(String bpmnXml) {
        return bpmnXml.toLowerCase().contains("startevent") || 
               bpmnXml.toLowerCase().contains("start-event") ||
               bpmnXml.toLowerCase().contains("bpmn:startevent");
    }

    private boolean hasEndEvent(String bpmnXml) {
        return bpmnXml.toLowerCase().contains("endevent") || 
               bpmnXml.toLowerCase().contains("end-event") ||
               bpmnXml.toLowerCase().contains("bpmn:endevent");
    }

    private boolean isValidWorkflowName(String name) {
        if (name == null) return false;
        
        String trimmed = name.trim();
        return trimmed.length() >= 3 && 
               trimmed.length() <= 100 &&
               trimmed.matches("^[a-zA-Z0-9\\s\\-_]+$");
    }

    /**
     * Validation result containing errors and warnings
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }

        public static ValidationResult valid(List<String> warnings) {
            return new ValidationResult(true, List.of(), warnings);
        }

        public static ValidationResult invalid(List<String> errors, List<String> warnings) {
            return new ValidationResult(false, errors, warnings);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }

        public String getWarningMessage() {
            return String.join("; ", warnings);
        }
    }
}