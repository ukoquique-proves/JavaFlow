package com.javaflow.ui.views;

import com.javaflow.model.Workflow;
import com.javaflow.model.WorkflowExecution;
import com.javaflow.service.BotService;
import com.javaflow.service.WorkflowService;
import com.javaflow.ui.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Vista de Dashboard principal
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | JavaFlow")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final WorkflowService workflowService;
    private final BotService botService;

    public DashboardView(WorkflowService workflowService, BotService botService) {
        this.workflowService = workflowService;
        this.botService = botService;

        setSpacing(true);
        setPadding(true);

        add(new H2("Dashboard"));

        // Métricas principales
        HorizontalLayout metrics = createMetrics();
        add(metrics);

        // Ejecuciones recientes
        add(new H2("Ejecuciones Recientes"));
        VerticalLayout recentExecutions = createRecentExecutions();
        add(recentExecutions);
    }

    private HorizontalLayout createMetrics() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        try {
            // Use optimized query to prevent N+1 (loads all workflows once)
            List<Workflow> workflows = workflowService.getAllWorkflowsWithCreator();
            
            // Total workflows
            long totalWorkflows = workflows.size();
            layout.add(createMetricCard("Workflows", String.valueOf(totalWorkflows), "primary"));

            // Workflows activos
            long activeWorkflows = workflows.stream()
                    .filter(w -> w.getStatus() == com.javaflow.model.Workflow.WorkflowStatus.ACTIVE)
                    .count();
            layout.add(createMetricCard("Activos", String.valueOf(activeWorkflows), "success"));

            // Bots activos
            long activeBots = botService.getActiveBots().size();
            layout.add(createMetricCard("Bots", String.valueOf(activeBots), "contrast"));

            // Ejecuciones recientes
            long recentExecutions = workflowService.getRecentExecutions(24).size();
            layout.add(createMetricCard("Ejecuciones (24h)", String.valueOf(recentExecutions), "primary"));
        } catch (Exception e) {
            // Database not ready yet, show placeholder
            layout.add(createMetricCard("Workflows", "0", "primary"));
            layout.add(createMetricCard("Activos", "0", "success"));
            layout.add(createMetricCard("Bots", "0", "contrast"));
            layout.add(createMetricCard("Ejecuciones (24h)", "0", "primary"));
        }

        return layout;
    }

    private Div createMetricCard(String title, String value, String theme) {
        Div card = new Div();
        card.addClassNames(
            LumoUtility.Padding.LARGE,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Background.CONTRAST_5
        );
        card.setWidth("250px");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(
            LumoUtility.FontSize.XXXLARGE,
            LumoUtility.FontWeight.BOLD
        );

        VerticalLayout content = new VerticalLayout(titleSpan, valueSpan);
        content.setSpacing(false);
        content.setPadding(false);

        card.add(content);
        return card;
    }

    private VerticalLayout createRecentExecutions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        try {
            var executions = workflowService.getRecentExecutions(24);
            
            if (executions.isEmpty()) {
                layout.add(new Span("No hay ejecuciones recientes"));
            } else {
                executions.stream()
                        .limit(10)
                        .forEach(execution -> {
                            HorizontalLayout row = new HorizontalLayout();
                            row.setWidthFull();
                            row.setAlignItems(Alignment.CENTER);

                            Span workflowName = new Span(execution.getWorkflow().getName());
                            Span status = new Span(execution.getStatus().toString());
                            status.getElement().getThemeList().add(getStatusBadge(execution.getStatus()));
                            
                            Span time = new Span(execution.getStartedAt().toString());
                            time.addClassNames(LumoUtility.TextColor.SECONDARY);

                            row.add(workflowName, status, time);
                            layout.add(row);
                        });
            }
        } catch (Exception e) {
            layout.add(new Span("No hay ejecuciones recientes"));
        }

        return layout;
    }

    private String getStatusBadge(WorkflowExecution.ExecutionStatus status) {
        return switch (status) {
            case RUNNING -> "badge success";
            case COMPLETED -> "badge";
            case FAILED -> "badge error";
            case CANCELLED -> "badge contrast";
            case SUSPENDED -> "badge contrast";
        };
    }
}
