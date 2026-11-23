package com.emergency.views;

import com.emergency.components.DashboardCard;
import com.emergency.components.Sidebar;
import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Citizen Main Dashboard Shell with Fixed Left Sidebar.
 */
public class UserDashboardView {

    private BorderPane root;
    private Sidebar sidebar;
    private Button dashboardButton, reportEmergencyButton, myRequestsButton, trackEmergencyButton, historyButton, notificationButton, quickReportButton;
    private Label welcomeLabel, notificationBadgeLabel;
    private DashboardCard totalCard, pendingCard, resolvedCard;
    private TableView<Emergency> recentActivityTable;
    private Node homeContent;

    public UserDashboardView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        try {
            root.getStylesheets().add(getClass().getResource("/com/emergency/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        sidebar = new Sidebar("Citizen User", "CITIZEN ACCOUNT");
        dashboardButton = new Button("Dashboard");
        reportEmergencyButton = new Button("Report Emergency");
        myRequestsButton = new Button("My Requests");
        trackEmergencyButton = new Button("Track Emergency");
        historyButton = new Button("Emergency History");
        notificationButton = new Button("Notifications");

        sidebar.addNavButton(dashboardButton);
        sidebar.addNavButton(reportEmergencyButton);
        sidebar.addNavButton(myRequestsButton);
        sidebar.addNavButton(trackEmergencyButton);
        sidebar.addNavButton(historyButton);
        sidebar.addNavButton(notificationButton);
        sidebar.setActiveButton(dashboardButton);
        root.setLeft(sidebar);

        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        Label headerTitle = new Label("Citizen Emergency Portal");
        headerTitle.getStyleClass().add("top-header-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        notificationBadgeLabel = new Label("0 Alerts");
        notificationBadgeLabel.getStyleClass().add("user-badge");
        Label statusLive = new Label("● System Operational");
        statusLive.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 12px;");

        topBar.getChildren().addAll(headerTitle, spacer, statusLive, notificationBadgeLabel);
        root.setTop(topBar);

        homeContent = createHomeContent();
        showDashboardHome();
    }

    private Node createHomeContent() {
        VBox contentBox = new VBox(16);
        contentBox.getStyleClass().add("content-pane");

        welcomeLabel = new Label("Welcome back!");
        welcomeLabel.getStyleClass().add("page-title");
        Label welcomeSub = new Label("Overview of your reported incidents and live response units.");
        welcomeSub.getStyleClass().add("page-subtitle");

        totalCard = new DashboardCard("TOTAL REPORTED", "0", "All incidents", "stat-card-blue");
        pendingCard = new DashboardCard("REQUESTED & ACTIVE", "0", "In queue / active", "stat-card-amber");
        resolvedCard = new DashboardCard("RESOLVED", "0", "Completed responses", "stat-card-green");

        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(pendingCard, Priority.ALWAYS);
        HBox.setHgrow(resolvedCard, Priority.ALWAYS);
        HBox cardsRow = new HBox(14, totalCard, pendingCard, resolvedCard);

        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(14, 18, 14, 18));
        banner.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 10px; -fx-border-color: #FCA5A5; -fx-border-radius: 10px;");

        VBox bannerText = new VBox(2,
                new Label("Need Immediate Emergency Assistance?"),
                new Label("Report medical, fire or rescue emergencies for review & allocation."));
        ((Label) bannerText.getChildren().get(0)).setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #991B1B;");
        ((Label) bannerText.getChildren().get(1)).setStyle("-fx-font-size: 11px; -fx-text-fill: #7F1D1D;");
        HBox.setHgrow(bannerText, Priority.ALWAYS);

        quickReportButton = new Button("Report Emergency Now");
        quickReportButton.getStyleClass().add("btn-danger");
        quickReportButton.setPrefHeight(36);
        banner.getChildren().addAll(bannerText, quickReportButton);

        VBox tableCard = new VBox(8);
        tableCard.getStyleClass().add("card");
        Label tableTitle = new Label("Your Recent Emergencies");
        tableTitle.getStyleClass().add("section-title");

        recentActivityTable = new TableView<>();
        recentActivityTable.setPrefHeight(200);
        recentActivityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(60);

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

        recentActivityTable.getColumns().addAll(idCol, typeCol, prioCol, locCol, stCol);
        recentActivityTable.setPlaceholder(new Label("No recent emergencies reported yet."));
        tableCard.getChildren().addAll(tableTitle, recentActivityTable);

        contentBox.getChildren().addAll(new VBox(2, welcomeLabel, welcomeSub), cardsRow, banner, tableCard);
        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    public void setContent(Node content) { root.setCenter(content); }
    public void showDashboardHome() { setContent(homeContent); sidebar.setActiveButton(dashboardButton); }
    public void updateSummary(int total, int pending, int resolved, int unread) {
        totalCard.setValue(total); pendingCard.setValue(pending); resolvedCard.setValue(resolved);
        notificationBadgeLabel.setText(unread > 0 ? unread + " New Alerts" : "No Alerts");
    }

    public BorderPane getView() { return root; }
    public Sidebar getSidebar() { return sidebar; }
    public Button getDashboardButton() { return dashboardButton; }
    public Button getReportEmergencyButton() { return reportEmergencyButton; }
    public Button getMyRequestsButton() { return myRequestsButton; }
    public Button getTrackEmergencyButton() { return trackEmergencyButton; }
    public Button getHistoryButton() { return historyButton; }
    public Button getNotificationButton() { return notificationButton; }
    public Button getLogoutButton() { return sidebar.getLogoutButton(); }
    public Button getQuickReportButton() { return quickReportButton; }
    public Label getWelcomeLabel() { return welcomeLabel; }
    public TableView<Emergency> getRecentActivityTable() { return recentActivityTable; }
}
