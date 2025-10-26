package com.javaflow.ui.views;

import com.javaflow.model.BotConfiguration;
import com.javaflow.service.BotService;
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
 * Vista de gestión de Bots
 */
@Route(value = "bots", layout = MainLayout.class)
@PageTitle("Bots | JavaFlow")
@PermitAll
public class BotManagementView extends VerticalLayout {

    private final BotService botService;
    private final Grid<BotConfiguration> grid;

    public BotManagementView(BotService botService) {
        this.botService = botService;

        setSpacing(true);
        setPadding(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Gestión de Bots");
        
        Button createButton = new Button("Nuevo Bot", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createBot());

        header.add(title, createButton);
        add(header);

        // Grid
        grid = new Grid<>(BotConfiguration.class, false);
        grid.addColumn(BotConfiguration::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(BotConfiguration::getName).setHeader("Nombre").setFlexGrow(1);
        grid.addColumn(BotConfiguration::getType).setHeader("Tipo").setWidth("120px");
        grid.addComponentColumn(this::createStatusBadge).setHeader("Estado").setWidth("120px");
        grid.addColumn(b -> b.getCreatedAt().toString()).setHeader("Creado").setWidth("180px");

        grid.addComponentColumn(this::createActionButtons).setHeader("Acciones").setWidth("250px");

        grid.setItems(botService.getAllBots());
        grid.setHeightFull();

        add(grid);
        setSizeFull();
    }

    private Span createStatusBadge(BotConfiguration bot) {
        Span badge = new Span(bot.getStatus().toString());
        badge.getElement().getThemeList().add("badge");
        
        switch (bot.getStatus()) {
            case ACTIVE:
                badge.getElement().getThemeList().add("success");
                break;
            case INACTIVE:
                badge.getElement().getThemeList().add("contrast");
                break;
            case ERROR:
                badge.getElement().getThemeList().add("error");
                break;
        }
        
        return badge;
    }

    private HorizontalLayout createActionButtons(BotConfiguration bot) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Botón Activar/Desactivar
        Button toggleButton;
        if (bot.getStatus() == BotConfiguration.BotStatus.ACTIVE) {
            toggleButton = new Button("Desactivar", VaadinIcon.PAUSE.create());
            toggleButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
            toggleButton.addClickListener(e -> deactivateBot(bot));
        } else {
            toggleButton = new Button("Activar", VaadinIcon.PLAY.create());
            toggleButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            toggleButton.addClickListener(e -> activateBot(bot));
        }

        // Botón Ver mensajes
        Button messagesButton = new Button("Mensajes", VaadinIcon.CHAT.create());
        messagesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        messagesButton.addClickListener(e -> viewMessages(bot));

        // Botón Editar
        Button editButton = new Button("Editar", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editButton.addClickListener(e -> editBot(bot));

        actions.add(toggleButton, messagesButton, editButton);
        return actions;
    }

    private void createBot() {
        Notification.show("Función de crear bot en desarrollo");
        // TODO: Abrir dialog para crear bot
    }

    private void activateBot(BotConfiguration bot) {
        try {
            botService.activateBot(bot.getId());
            Notification.show("Bot activado: " + bot.getName());
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void deactivateBot(BotConfiguration bot) {
        try {
            botService.deactivateBot(bot.getId());
            Notification.show("Bot desactivado: " + bot.getName());
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void viewMessages(BotConfiguration bot) {
        Notification.show("Ver mensajes de: " + bot.getName());
        // TODO: Navegar a vista de mensajes
    }

    private void editBot(BotConfiguration bot) {
        Notification.show("Editar bot: " + bot.getName());
        // TODO: Abrir dialog de edición
    }

    private void refreshGrid() {
        grid.setItems(botService.getAllBots());
    }
}
