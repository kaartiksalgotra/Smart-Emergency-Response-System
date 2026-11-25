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
 * ERAS Admin Dashboard View Component.
 * Unified horizontal blue gradient stat banner and centered 1200px container.
 */
public class AdminDashboardView {
    private BorderPane root;
    private Sidebar sidebar;
    private Button dashboardButton, incomingRequestsButton, manageResourcesButton, allocationButton, monitorEmergencyButton, refreshButton;
    private Label totalVal, requestedVal, readinessVal, avgResponseVal;
    private Label medCountLabel, fireCountLabel, resCountLabel, otherCountLabel;
    private TableView<Emergency> emergencyTable;
    private VBox bodyBox;
    private ScrollPane homeScroll;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public AdminDashboardView() {
        root = new BorderPane(); root.setStyle("-fx-background-color: #F8FAFC;");
        sidebar = new Sidebar("System Administrator", "ADMINISTRATOR");
        dashboardButton = new Button("Dashboard"); incomingRequestsButton = new Button("Incoming Requests");
        manageResourcesButton = new Button("Resource Management");
        allocationButton = new Button("Resource Allocation"); monitorEmergencyButton = new Button("Monitor Emergencies");
        for (Button b : new Button[]{dashboardButton, incomingRequestsButton, manageResourcesButton, allocationButton, monitorEmergencyButton}) {
            sidebar.addNavButton(b);
        }
        sidebar.setActiveButton(dashboardButton); root.setTop(sidebar);
        bodyBox = new VBox(); homeScroll = createHomeScroll(); showDashboardHome(); root.setCenter(bodyBox);
    }

    private ScrollPane createHomeScroll() {
        Label title = new Label("Admin Command Dashboard"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("Citywide emergency oversight & operational readiness"); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        refreshButton = new Button("🔄 Refresh All"); refreshButton.getStyleClass().add("btn-primary");
        HBox header = new HBox(12, new VBox(2, title, sub), sp, refreshButton); header.setAlignment(Pos.CENTER_LEFT);

        totalVal = new Label("0"); requestedVal = new Label("0"); readinessVal = new Label("92%"); avgResponseVal = new Label("8.4m");
        HBox banner = new HBox(0,
            makeBannerCol("CITYWIDE INCIDENTS", totalVal, "Total logged incidents"), makeDivider(),
            makeBannerCol("PENDING REQUESTS", requestedVal, "Awaiting triage / dispatch"), makeDivider(),
            makeBannerCol("UNIT READINESS", readinessVal, "Active fleet operational"), makeDivider(),
            makeBannerCol("AVG RESPONSE TIME", avgResponseVal, "Median response time")
        );
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: linear-gradient(to bottom right, #38BDF8, #2563EB); -fx-background-radius: 18px; -fx-padding: 22 28;");

        buildEmergencyTable();
        VBox tableCard = new VBox(10, new Label("Live Emergency Incident Feed") {{ setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); }}, emergencyTable);
        tableCard.setPadding(new Insets(18)); tableCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        VBox sideCard = buildSideCard(); sideCard.setPrefWidth(300); sideCard.setMinWidth(300);
        HBox centerRow = new HBox(16, tableCard, sideCard);

        VBox content = new VBox(18, header, banner, centerRow);
        content.setMaxWidth(1200);
        content.setPadding(new Insets(20, 32, 24, 32));

        StackPane centerWrapper = new StackPane(content);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    private VBox makeBannerCol(String title, Label valLbl, String sub) {
        Label t = new Label(title); t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.85); -fx-letter-spacing: 0.5px;");
        valLbl.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label s = new Label(sub); s.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
        VBox col = new VBox(4, t, valLbl, s); col.setAlignment(Pos.CENTER_LEFT); col.setPadding(new Insets(0, 16, 0, 16));
        HBox.setHgrow(col, Priority.ALWAYS); return col;
    }

    private Region makeDivider() {
        Region div = new Region(); div.setPrefWidth(1); div.setMaxWidth(1); div.setPrefHeight(60);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.25);"); return div;
    }

    @SuppressWarnings("unchecked")
    private void buildEmergencyTable() {
        emergencyTable = new TableView<>(); emergencyTable.setPrefHeight(280);
        emergencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(60);

        TableColumn<Emergency, String> userCol = new TableColumn<>("CITIZEN");
        userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));

        TableColumn<Emergency, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));
        typeCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createTypeBadge(e.getEmergencyType()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> prioCol = new TableColumn<>("PRIORITY");
        prioCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        prioCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createPriorityBadge(e.getPriority()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("LOCATION");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> stCol = new TableColumn<>("STATUS");
        stCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createStatusBadge(e.getStatus()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> timeCol = new TableColumn<>("TIME");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(TIME_FMT) : "-"));

        emergencyTable.getColumns().addAll(idCol, userCol, typeCol, prioCol, locCol, stCol, timeCol);
        emergencyTable.setPlaceholder(new Label("No active emergency incidents logged in the system."));
    }

    private VBox buildSideCard() {
        VBox card = new VBox(12); card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");
        Label t = new Label("Incident Distribution"); t.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        medCountLabel = new Label("0"); fireCountLabel = new Label("0"); resCountLabel = new Label("0"); otherCountLabel = new Label("0");
        VBox list = new VBox(10,
            makeDistRow("Medical Incidents", medCountLabel, "#2563EB", "#EFF6FF"),
            makeDistRow("Fire Outbreaks", fireCountLabel, "#EF4444", "#FEF2F2"),
            makeDistRow("Rescue Operations", resCountLabel, "#D97706", "#FFFBEB"),
            makeDistRow("Other / Hazmat", otherCountLabel, "#64748B", "#F1F5F9")
        );
        card.getChildren().addAll(t, list); return card;
    }

    private HBox makeDistRow(String name, Label val, String textCol, String bgCol) {
        Label n = new Label(name); n.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E293B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        val.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + textCol + "; -fx-background-color: " + bgCol + "; -fx-padding: 3 8; -fx-background-radius: 6px;");
        HBox r = new HBox(8, n, sp, val); r.setAlignment(Pos.CENTER_LEFT); return r;
    }

    public void setContent(Node content) { bodyBox.getChildren().setAll(content); VBox.setVgrow(content, Priority.ALWAYS); }
    public void showDashboardHome() { setContent(homeScroll); sidebar.setActiveButton(dashboardButton); }

    public void updateStatistics(int total, int req, int acc, int assign, int resol, int cancel, int totalRes, int availRes, int allocRes) {
        totalVal.setText(String.valueOf(total)); requestedVal.setText(String.valueOf(req));
        if (totalRes > 0) {
            int pct = (availRes * 100) / totalRes;
            readinessVal.setText(pct + "%");
        }
    }

    public void updateDashboardMetrics(int total, int req, int assigned, int inProg, int resolved, int med, int fire, int res, int other) {
        totalVal.setText(String.valueOf(total)); requestedVal.setText(String.valueOf(req));
        medCountLabel.setText(String.valueOf(med)); fireCountLabel.setText(String.valueOf(fire));
        resCountLabel.setText(String.valueOf(res)); otherCountLabel.setText(String.valueOf(other));
    }

    public Parent getView() { return root; } public Sidebar getSidebar() { return sidebar; } public TableView<Emergency> getEmergencyTable() { return emergencyTable; }
    public Button getDashboardButton() { return dashboardButton; } public Button getIncomingRequestsButton() { return incomingRequestsButton; }
    public Button getManageResourcesButton() { return manageResourcesButton; }
    public Button getAllocationButton() { return allocationButton; } public Button getMonitorEmergencyButton() { return monitorEmergencyButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getLogoutButton() { return sidebar.getLogoutButton(); }
}
