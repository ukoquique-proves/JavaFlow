package com.javaflow.ui.views;

import com.javaflow.application.dto.workflow.WorkflowResponse;
import com.javaflow.model.Workflow;
import com.javaflow.service.WorkflowService;
import com.javaflow.ui.MainLayout;

import java.util.List;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/**
 * Vista de lista de Workflows
 */
@Route(value = "workflows", layout = MainLayout.class)
@PageTitle("Workflows | JavaFlow")
@PermitAll
public class WorkflowListView extends VerticalLayout {

    private final WorkflowService workflowService;
    private final Grid<WorkflowResponse> grid;

    public WorkflowListView(WorkflowService workflowService) {
        this.workflowService = workflowService;

        setSpacing(true);
        setPadding(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Workflows");
        
        Button createButton = new Button("Nuevo Workflow", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createWorkflow());

        header.add(title, createButton);
        add(header);

        // Grid
        grid = new Grid<>(WorkflowResponse.class, false);
        grid.addColumn(WorkflowResponse::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(WorkflowResponse::getName).setHeader("Nombre").setFlexGrow(1);
        grid.addColumn(WorkflowResponse::getDescription).setHeader("Descripción").setFlexGrow(2);
        grid.addColumn(WorkflowResponse::getStatus).setHeader("Estado").setWidth("120px");
        grid.addColumn(WorkflowResponse::getVersion).setHeader("Versión").setWidth("100px");
        grid.addColumn(w -> w.getCreatedAt().toString()).setHeader("Creado").setWidth("180px");
        grid.addColumn(WorkflowResponse::getSuccessRate).setHeader("Éxito %").setWidth("100px");

        grid.addComponentColumn(this::createActionButtons).setHeader("Acciones").setWidth("300px");

        refreshGrid();
        grid.setHeightFull();

        add(grid);
        setSizeFull();
    }

    private HorizontalLayout createActionButtons(WorkflowResponse workflow) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Botón Ejecutar
        Button executeButton = new Button("Ejecutar", VaadinIcon.PLAY.create());
        executeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        executeButton.addClickListener(e -> executeWorkflow(workflow));
        executeButton.setEnabled("ACTIVE".equals(workflow.getStatus()));

        // Botón Activar/Desactivar
        Button toggleButton;
        if ("ACTIVE".equals(workflow.getStatus())) {
            toggleButton = new Button("Desactivar", VaadinIcon.PAUSE.create());
            toggleButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
            toggleButton.addClickListener(e -> deactivateWorkflow(workflow));
        } else {
            toggleButton = new Button("Activar", VaadinIcon.CHECK.create());
            toggleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            toggleButton.addClickListener(e -> activateWorkflow(workflow));
        }

        // Botón Ver
        Button viewButton = new Button("Ver", VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewButton.addClickListener(e -> viewWorkflow(workflow));

        actions.add(executeButton, toggleButton, viewButton);
        return actions;
    }

    private void createWorkflow() {
        Notification.show("Función de crear workflow en desarrollo");
        // TODO: Abrir dialog para crear workflow
    }

    private void executeWorkflow(WorkflowResponse workflow) {
        try {
            // Use the new use case pattern
            workflowService.executeWorkflowWithUseCase(workflow.getId(), java.util.Map.of(), null);
            Notification.show("Workflow ejecutado: " + workflow.getName());
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void activateWorkflow(WorkflowResponse workflow) {
        try {
            // Use the new use case pattern
            workflowService.activateWorkflowWithUseCase(workflow.getId());
            Notification.show("Workflow activado: " + workflow.getName());
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void deactivateWorkflow(WorkflowResponse workflow) {
        try {
            workflowService.deactivateWorkflow(workflow.getId());
            Notification.show("Workflow desactivado: " + workflow.getName());
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void viewWorkflow(WorkflowResponse workflow) {
        Notification.show("Ver detalles de: " + workflow.getName());
        // TODO: Navegar a vista de detalles
    }

    private void refreshGrid() {
        List<Workflow> workflows = workflowService.getAllWorkflows();
        List<WorkflowResponse> responses = workflows.stream()
                .map(WorkflowResponse::from)
                .toList();
        grid.setItems(responses);
    }
}
