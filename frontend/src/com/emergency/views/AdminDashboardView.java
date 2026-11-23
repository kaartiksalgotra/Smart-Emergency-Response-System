package com.emergency.views;

import com.emergency.components.DashboardCard;
import com.emergency.components.Sidebar;
import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Emergency Command Center View with Fixed Left Sidebar.
 */
public class AdminDashboardView {

    private BorderPane root;
    private Sidebar sidebar;
    private Button dashboardButton, incomingRequestsButton, priorityQueueButton, manageResourcesButton, allocationButton, monitorEmergencyButton, reportsButton, refreshButton;
    private TableView<Emergency> emergencyTable;
    private DashboardCard totalCard, requestedCard, acceptedCard, assignedCard, resolvedCard, cancelledCard;
    private DashboardCard totalResourcesCard, availableResourcesCard, allocatedResourcesCard;
    private Node homeContent;

    public AdminDashboardView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        try {
            root.getStylesheets().add(getClass().getResource("/com/emergency/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        sidebar = new Sidebar("System Administrator", "ADMINISTRATOR");
        dashboardButton = new Button("Dashboard");
        incomingRequestsButton = new Button("Incoming Requests");
        priorityQueueButton = new Button("Priority Queue");
        manageResourcesButton = new Button("Resource Management");
        allocationButton = new Button("Resource Allocation");
        monitorEmergencyButton = new Button("Monitor Emergencies");
        reportsButton = new Button("Reports & Analytics");

        sidebar.addNavButton(dashboardButton);
        sidebar.addNavButton(incomingRequestsButton);
        sidebar.addNavButton(priorityQueueButton);
        sidebar.addNavButton(manageResourcesButton);
        sidebar.addNavButton(allocationButton);
        sidebar.addNavButton(monitorEmergencyButton);
        sidebar.addNavButton(reportsButton);
        sidebar.setActiveButton(dashboardButton);
        root.setLeft(sidebar);

        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        Label title = new Label("Emergency Command Center");
        title.getStyleClass().add("top-header-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh All");
        refreshButton.getStyleClass().add("btn-primary");
        topBar.getChildren().addAll(title, spacer, refreshButton);
        root.setTop(topBar);

        homeContent = createHomeContent();
        showDashboardHome();
    }

    private Node createHomeContent() {
        VBox contentBox = new VBox(14);
        contentBox.getStyleClass().add("content-pane");

        totalCard = new DashboardCard("TOTAL INCIDENTS", "0", "stat-card-blue");
        requestedCard = new DashboardCard("REQUESTED", "0", "stat-card-amber");
        acceptedCard = new DashboardCard("ACCEPTED", "0", "stat-card-slate");
        assignedCard = new DashboardCard("ASSIGNED", "0", "stat-card-blue");
        resolvedCard = new DashboardCard("RESOLVED", "0", "stat-card-green");
        cancelledCard = new DashboardCard("CANCELLED", "0", "stat-card-red");

        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(requestedCard, Priority.ALWAYS);
        HBox.setHgrow(acceptedCard, Priority.ALWAYS);
        HBox.setHgrow(assignedCard, Priority.ALWAYS);
        HBox.setHgrow(resolvedCard, Priority.ALWAYS);
        HBox.setHgrow(cancelledCard, Priority.ALWAYS);
        HBox emCards = new HBox(8, totalCard, requestedCard, acceptedCard, assignedCard, resolvedCard, cancelledCard);

        totalResourcesCard = new DashboardCard("TOTAL UNITS", "0", "stat-card-slate");
        availableResourcesCard = new DashboardCard("AVAILABLE", "0", "stat-card-green");
        allocatedResourcesCard = new DashboardCard("ALLOCATED", "0", "stat-card-amber");

        HBox.setHgrow(totalResourcesCard, Priority.ALWAYS);
        HBox.setHgrow(availableResourcesCard, Priority.ALWAYS);
        HBox.setHgrow(allocatedResourcesCard, Priority.ALWAYS);
        HBox resCards = new HBox(12, totalResourcesCard, availableResourcesCard, allocatedResourcesCard);

        VBox tableCard = new VBox(8);
        tableCard.getStyleClass().add("card");
        Label tableTitle = new Label("Live Incident Dispatch Overview");
        tableTitle.getStyleClass().add("section-title");

        emergencyTable = new TableView<>();
        emergencyTable.setPrefHeight(250);
        emergencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(60);

        TableColumn<Emergency, String> userCol = new TableColumn<>("Citizen ID");
        userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));

        TableColumn<Emergency, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));

        TableColumn<Emergency, String> prioCol = new TableColumn<>("Priority");
        prioCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        prioCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); setText(null); }
                else { setGraphic(StatusIndicator.createPriorityBadge(((Emergency) getTableRow().getItem()).getPriority())); setText(null); setAlignment(Pos.CENTER); }
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> stCol = new TableColumn<>("Status");
        stCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); setText(null); }
                else { setGraphic(StatusIndicator.createStatusBadge(((Emergency) getTableRow().getItem()).getStatus())); setText(null); setAlignment(Pos.CENTER); }
            }
        });

        emergencyTable.getColumns().addAll(idCol, userCol, typeCol, prioCol, locCol, stCol);
        tableCard.getChildren().addAll(tableTitle, emergencyTable);

        contentBox.getChildren().addAll(new Label("EMERGENCY REQUEST METRICS"), emCards, new Label("RESOURCE AVAILABILITY"), resCards, tableCard);
        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    public void setContent(Node content) { root.setCenter(content); }
    public void showDashboardHome() { setContent(homeContent); sidebar.setActiveButton(dashboardButton); }

    public void updateStatistics(int total, int req, int acc, int assign, int resol, int cancel, int totalRes, int availRes, int allocRes) {
        totalCard.setValue(total); requestedCard.setValue(req); acceptedCard.setValue(acc);
        assignedCard.setValue(assign); resolvedCard.setValue(resol); cancelledCard.setValue(cancel);
        totalResourcesCard.setValue(totalRes); availableResourcesCard.setValue(availRes); allocatedResourcesCard.setValue(allocRes);
    }

    public Parent getView() { return root; }
    public Sidebar getSidebar() { return sidebar; }
    public TableView<Emergency> getEmergencyTable() { return emergencyTable; }
    public Button getDashboardButton() { return dashboardButton; }
    public Button getIncomingRequestsButton() { return incomingRequestsButton; }
    public Button getPriorityQueueButton() { return priorityQueueButton; }
    public Button getManageResourcesButton() { return manageResourcesButton; }
    public Button getAllocationButton() { return allocationButton; }
    public Button getMonitorEmergencyButton() { return monitorEmergencyButton; }
    public Button getReportsButton() { return reportsButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getLogoutButton() { return sidebar.getLogoutButton(); }
}
