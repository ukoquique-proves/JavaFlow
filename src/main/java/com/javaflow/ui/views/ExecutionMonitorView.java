package com.javaflow.ui.views;

import com.javaflow.model.WorkflowExecution;
import com.javaflow.service.WorkflowService;
import com.javaflow.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/**
 * Vista de monitoreo de ejecuciones
 */
@Route(value = "executions", layout = MainLayout.class)
@PageTitle("Ejecuciones | JavaFlow")
@PermitAll
public class ExecutionMonitorView extends VerticalLayout {

    private final WorkflowService workflowService;
    private final Grid<WorkflowExecution> grid;

    public ExecutionMonitorView(WorkflowService workflowService) {
        this.workflowService = workflowService;

        setSpacing(true);
        setPadding(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Monitor de Ejecuciones");
        
        Button refreshButton = new Button("Actualizar", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> refreshGrid());

        header.add(title, refreshButton);
        add(header);

        // Grid
        grid = new Grid<>(WorkflowExecution.class, false);
        grid.addColumn(WorkflowExecution::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(e -> e.getWorkflow().getName()).setHeader("Workflow").setFlexGrow(1);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Estado").setWidth("120px");
        grid.addColumn(e -> e.getStartedAt().toString()).setHeader("Inicio").setWidth("180px");
        grid.addColumn(e -> e.getEndedAt() != null ? e.getEndedAt().toString() : "-")
                .setHeader("Fin").setWidth("180px");
        grid.addColumn(e -> e.getStartedBy() != null ? e.getStartedBy().getUsername() : "Sistema")
                .setHeader("Iniciado por").setWidth("150px");

        grid.addComponentColumn(this::createActionButtons).setHeader("Acciones").setWidth("150px");

        grid.setItems(workflowService.getRecentExecutions(168)); // Última semana
        grid.setHeightFull();

        add(grid);
        setSizeFull();
    }

    private Span createStatusBadge(WorkflowExecution execution) {
        Span badge = new Span(execution.getStatus().toString());
        badge.getElement().getThemeList().add("badge");
        
        switch (execution.getStatus()) {
            case RUNNING:
                badge.getElement().getThemeList().add("success");
                break;
            case COMPLETED:
                badge.getElement().getThemeList().add("primary");
                break;
            case FAILED:
                badge.getElement().getThemeList().add("error");
                break;
            case CANCELLED:
            case SUSPENDED:
                badge.getElement().getThemeList().add("contrast");
                break;
        }
        
        return badge;
    }

    private HorizontalLayout createActionButtons(WorkflowExecution execution) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Botón Ver detalles
        Button viewButton = new Button("Ver", VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewButton.addClickListener(e -> viewExecution(execution));

        // Botón Cancelar (solo si está corriendo)
        if (execution.getStatus() == WorkflowExecution.ExecutionStatus.RUNNING) {
            Button cancelButton = new Button("Cancelar", VaadinIcon.CLOSE.create());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            cancelButton.addClickListener(e -> cancelExecution(execution));
            actions.add(viewButton, cancelButton);
        } else {
            actions.add(viewButton);
        }

        return actions;
    }

    private void viewExecution(WorkflowExecution execution) {
        Notification.show("Ver detalles de ejecución: " + execution.getId());
        // TODO: Abrir dialog con detalles
    }

    private void cancelExecution(WorkflowExecution execution) {
        try {
            workflowService.cancelExecution(execution.getId());
            Notification.show("Ejecución cancelada");
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void refreshGrid() {
        grid.setItems(workflowService.getRecentExecutions(168));
    }
}
