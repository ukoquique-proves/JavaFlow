package com.javaflow.ui;

import com.javaflow.ui.views.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Layout principal de la aplicación Vaadin
 */
@PageTitle("JavaFlow")
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("JavaFlow");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );

        Span version = new Span("v1.0.0");
        version.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(),
            logo,
            version
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        // Dashboard
        nav.addItem(new SideNavItem(
            "Dashboard",
            DashboardView.class,
            VaadinIcon.DASHBOARD.create()
        ));

        // Workflows
        nav.addItem(new SideNavItem(
            "Workflows",
            WorkflowListView.class,
            VaadinIcon.AUTOMATION.create()
        ));

        // Executions
        nav.addItem(new SideNavItem(
            "Ejecuciones",
            ExecutionMonitorView.class,
            VaadinIcon.PLAY.create()
        ));

        // Bots
        nav.addItem(new SideNavItem(
            "Bots",
            BotManagementView.class,
            VaadinIcon.CHAT.create()
        ));

        // Logs
        nav.addItem(new SideNavItem(
            "Logs",
            LogsView.class,
            VaadinIcon.LIST.create()
        ));

        addToDrawer(nav);
    }
}
