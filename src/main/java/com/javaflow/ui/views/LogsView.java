package com.javaflow.ui.views;

import com.javaflow.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "logs", layout = MainLayout.class)
@PageTitle("Logs | JavaFlow")
@PermitAll
public class LogsView extends VerticalLayout {
    public LogsView() {
        add(new H2("Logs del Sistema"));
    }
}
