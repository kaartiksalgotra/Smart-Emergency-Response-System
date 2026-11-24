package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * ERAS Reports & Analytics View.
 * Modern export dashboard centered in 1200px container with uniform button styling.
 */
public class ReportsView {
    private BorderPane root;
    private Button allEmergenciesButton, pendingEmergenciesButton, resolvedEmergenciesButton, allocationReportButton, resourceReportButton, backButton;
    private Label messageLabel;

    public ReportsView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        backButton = new Button("← Back to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Reports & Analytics Center");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminPill = new Label("Admin View");
        adminPill.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 12px; -fx-font-size: 11px;");

        HBox headerContent = new HBox(16, backButton, heading, spacer, adminPill);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Center Content (centered, max-width 880px)
        VBox contentBox = new VBox(18);
        contentBox.setPadding(new Insets(28, 20, 36, 20));
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setMaxWidth(880);

        messageLabel = new Label("Click an export button below to generate a standardized CSV report.");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: #EFF6FF; -fx-padding: 12px 18px; -fx-background-radius: 8px; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-border-color: #BFDBFE; -fx-border-radius: 8px;");
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        allEmergenciesButton = createExportBtn();
        pendingEmergenciesButton = createExportBtn();
        resolvedEmergenciesButton = createExportBtn();
        allocationReportButton = createExportBtn();
        resourceReportButton = createExportBtn();

        VBox reportsCard = new VBox(14,
                makeReportRow("All Emergencies Master Log", "Export full history of all submitted emergency incidents.", allEmergenciesButton),
                makeReportRow("Pending Emergencies Only", "Export open, active, or in-progress emergency records.", pendingEmergenciesButton),
                makeReportRow("Resolved Emergencies History", "Export historical records of all successfully resolved incidents.", resolvedEmergenciesButton),
                makeReportRow("Resource Allocations Ledger", "Export all dispatch assignments, units, and active missions.", allocationReportButton),
                makeReportRow("Fleet & Resource Inventory", "Export current status, capacity, and readiness of all response units.", resourceReportButton)
        );
        reportsCard.getStyleClass().add("card");
        reportsCard.setPadding(new Insets(24));

        contentBox.getChildren().addAll(messageLabel, reportsCard);

        StackPane centerWrapper = new StackPane(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    private Button createExportBtn() {
        Button b = new Button("📥 Export CSV");
        b.getStyleClass().add("btn-secondary");
        return b;
    }

    private HBox makeReportRow(String title, String desc, Button btn) {
        Label t = new Label(title); t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
        Label d = new Label(desc); d.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        VBox texts = new VBox(2, t, d);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox row = new HBox(12, texts, sp, btn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
        return row;
    }

    public Parent getView() { return root; }
    public Button getAllEmergenciesButton() { return allEmergenciesButton; }
    public Button getPendingEmergenciesButton() { return pendingEmergenciesButton; }
    public Button getResolvedEmergenciesButton() { return resolvedEmergenciesButton; }
    public Button getAllocationReportButton() { return allocationReportButton; }
    public Button getResourceReportButton() { return resourceReportButton; }
    public Button getBackButton() { return backButton; }
    public Label getMessageLabel() { return messageLabel; }
}
