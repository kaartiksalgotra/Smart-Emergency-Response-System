package com.emergency.views;

import com.emergency.components.Sidebar;
import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;

/**
 * ERAS Citizen User Dashboard View Component.
 * Unified horizontal blue gradient stat banner and centered 1200px container.
 */
public class UserDashboardView {
    private BorderPane root;
    private Sidebar sidebar;
    private Button dashboardButton, reportEmergencyButton, myEmergenciesButton, notificationButton, quickReportButton;
    private Label welcomeLabel, notifBadgeLabel, totalVal, pendingVal, resolvedVal, unreadVal;
    private TableView<Emergency> recentActivityTable;
    private VBox bodyBox;
    private ScrollPane homeScroll;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public UserDashboardView() {
        root = new BorderPane(); root.setStyle("-fx-background-color: #F8FAFC;");
        sidebar = new Sidebar("Citizen User", "RESIDENT");
        dashboardButton = new Button("Dashboard"); reportEmergencyButton = new Button("Report Emergency");
        myEmergenciesButton = new Button("My Emergencies");
        notificationButton = new Button("Notifications");
        for (Button b : new Button[]{dashboardButton, reportEmergencyButton, myEmergenciesButton, notificationButton}) {
            sidebar.addNavButton(b);
        }
        sidebar.setActiveButton(dashboardButton); root.setTop(sidebar);
        bodyBox = new VBox(); homeScroll = createHomeScroll(); showDashboardHome(); root.setCenter(bodyBox);
    }

    private HBox createHeader() {
        welcomeLabel = new Label("Welcome back!"); welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("● Emergency Services Network: Normal Operations - GPS Synced"); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        notifBadgeLabel = new Label("0 Unread");
        notifBadgeLabel.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 12px; -fx-font-size: 11px;");
        quickReportButton = new Button("🚨 REPORT EMERGENCY"); quickReportButton.getStyleClass().add("btn-danger");
        HBox bar = new HBox(14, new VBox(2, welcomeLabel, sub), sp, notifBadgeLabel, quickReportButton);
        bar.setAlignment(Pos.CENTER_LEFT); return bar;
    }

    private ScrollPane createHomeScroll() {
        HBox header = createHeader();
        totalVal = new Label("0"); pendingVal = new Label("0"); resolvedVal = new Label("0"); unreadVal = new Label("0");

        HBox banner = new HBox(0,
            makeBannerCol("MY TOTAL REQUESTS", totalVal, "All time incidents"), makeDivider(),
            makeBannerCol("IN PROGRESS", pendingVal, "Awaiting or dispatched"), makeDivider(),
            makeBannerCol("RESOLVED", resolvedVal, "Completed safely"), makeDivider(),
            makeBannerCol("UNREAD ALERTS", unreadVal, "System notifications")
        );
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: linear-gradient(to bottom right, #38BDF8, #2563EB); -fx-background-radius: 18px; -fx-padding: 22 28;");

        Label tTitle = new Label("My Recent Emergency Requests"); tTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label tSub = new Label("Live status and dispatch timeline of submitted incidents"); tSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        buildTable();
        VBox tableCard = new VBox(10, tTitle, tSub, recentActivityTable); tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");

        HBox infoRow = new HBox(14,
            makeInfoCard("Real-Time Dispatch", "Connects citizens with verified civic services instantaneously.", "⚡"),
            makeInfoCard("Live Incident Tracking", "Monitor responder movement and milestones in real time.", "📍"),
            makeInfoCard("Secure & Encrypted", "Incident reports and personal data are encrypted end-to-end.", "🔒")
        );

        VBox content = new VBox(18, header, banner, tableCard, infoRow);
        content.setMaxWidth(1200); content.setPadding(new Insets(20, 32, 24, 32));

        StackPane centerWrapper = new StackPane(content);
        centerWrapper.setAlignment(Pos.TOP_CENTER); centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane sp = new ScrollPane(centerWrapper); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return sp;
    }

    private VBox makeBannerCol(String title, Label valLbl, String sub) {
        Label t = new Label(title); t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.85);");
        valLbl.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label s = new Label(sub); s.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
        VBox col = new VBox(4, t, valLbl, s); col.setAlignment(Pos.CENTER_LEFT); col.setPadding(new Insets(0, 20, 0, 20));
        HBox.setHgrow(col, Priority.ALWAYS); return col;
    }

    private Region makeDivider() {
        Region div = new Region(); div.setPrefWidth(1); div.setMaxWidth(1); div.setPrefHeight(60);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.25);"); return div;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        recentActivityTable = new TableView<>(); recentActivityTable.setPrefHeight(250);
        recentActivityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("INCIDENT ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId())); idCol.setMaxWidth(80);

        TableColumn<Emergency, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));
        typeCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createTypeBadge(e.getEmergencyType()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> prioCol = new TableColumn<>("PRIORITY");
        prioCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        prioCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createPriorityBadge(e.getPriority()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("LOCATION");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> stCol = new TableColumn<>("CURRENT STATUS");
        stCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createStatusBadge(e.getStatus()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> timeCol = new TableColumn<>("REPORTED AT");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(TIME_FMT) : "-"));

        recentActivityTable.getColumns().addAll(idCol, typeCol, prioCol, locCol, stCol, timeCol);
        recentActivityTable.setPlaceholder(makePlaceholder());
    }

    private HBox makeInfoCard(String title, String desc, String icon) {
        Label ic = new Label(icon); ic.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-background-radius: 8px; -fx-padding: 8 10; -fx-font-size: 14px;");
        Label t = new Label(title); t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
        Label d = new Label(desc); d.setWrapText(true); d.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        HBox card = new HBox(12, ic, new VBox(3, t, d)); card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");
        HBox.setHgrow(card, Priority.ALWAYS); card.setMaxWidth(Double.MAX_VALUE); return card;
    }

    private VBox makePlaceholder() {
        Label ic = new Label("📋"); ic.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 20px; -fx-padding: 8 12; -fx-font-size: 16px;");
        Label t = new Label("You have not reported any emergencies yet."); t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
        Label s = new Label("When you report an urgent incident, real-time dispatch progress will appear here."); s.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        VBox box = new VBox(6, ic, t, s); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(24)); return box;
    }

    public void setContent(Node content) { bodyBox.getChildren().setAll(content); VBox.setVgrow(content, Priority.ALWAYS); }
    public void showDashboardHome() { setContent(homeScroll); sidebar.setActiveButton(dashboardButton); }

    public void updateSummary(int total, int pending, int resolved, int unread) {
        totalVal.setText(String.valueOf(total)); pendingVal.setText(String.valueOf(pending));
        resolvedVal.setText(String.valueOf(resolved)); unreadVal.setText(String.valueOf(unread));
        notifBadgeLabel.setText(unread + " Unread");
    }

    public Parent getView() { return root; } public Sidebar getSidebar() { return sidebar; } public Label getWelcomeLabel() { return welcomeLabel; }
    public TableView<Emergency> getRecentActivityTable() { return recentActivityTable; } public Button getDashboardButton() { return dashboardButton; }
    public Button getReportEmergencyButton() { return reportEmergencyButton; } public Button getMyEmergenciesButton() { return myEmergenciesButton; }
    public Button getMyRequestsButton() { return myEmergenciesButton; } public Button getTrackEmergencyButton() { return myEmergenciesButton; }
    public Button getHistoryButton() { return myEmergenciesButton; } public Button getNotificationButton() { return notificationButton; }
    public Button getQuickReportButton() { return quickReportButton; } public Button getLogoutButton() { return sidebar.getLogoutButton(); }
}
