package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Reports and Analytics View Component.
 * Generates CSV audit reports for incidents, allocations, and resources.
 */
public class ReportsView {

    private BorderPane root;

    private Button allEmergenciesButton;
    private Button pendingEmergenciesButton;
    private Button resolvedEmergenciesButton;
    private Button allocationReportButton;
    private Button resourceReportButton;
    private Button backButton;

    private Label messageLabel;

    public ReportsView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Reports & Analytics Center");
        heading.getStyleClass().add("top-header-title");

        topBar.getChildren().addAll(backButton, heading);
        root.setTop(topBar);

        // Center Content
        VBox contentBox = new VBox(18);
        contentBox.getStyleClass().add("content-pane");
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setMaxWidth(850);

        messageLabel = new Label("Click an export button below to generate a standardized CSV report.");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: #EFF6FF; -fx-padding: 10px 16px; -fx-background-radius: 6px; -fx-text-fill: #1E40AF; -fx-font-weight: bold;");
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        allEmergenciesButton = new Button("Generate CSV");
        allEmergenciesButton.getStyleClass().add("btn-primary");

        pendingEmergenciesButton = new Button("Generate CSV");
        pendingEmergenciesButton.getStyleClass().add("btn-primary");

        resolvedEmergenciesButton = new Button("Generate CSV");
        resolvedEmergenciesButton.getStyleClass().add("btn-primary");

        allocationReportButton = new Button("Generate CSV");
        allocationReportButton.getStyleClass().add("btn-primary");

        resourceReportButton = new Button("Generate CSV");
        resourceReportButton.getStyleClass().add("btn-primary");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        VBox c1 = createCard("All Emergencies Report", "Full record of every reported incident.", allEmergenciesButton);
        VBox c2 = createCard("Pending Incidents Report", "Incidents awaiting dispatch/resolution.", pendingEmergenciesButton);
        VBox c3 = createCard("Resolved Incidents Report", "Successfully completed emergencies.", resolvedEmergenciesButton);
        VBox c4 = createCard("Resource Allocation Report", "Ledger of unit assignments to incidents.", allocationReportButton);
        VBox c5 = createCard("Fleet Inventory Report", "Current status of Ambulances, Trucks & Teams.", resourceReportButton);

        GridPane.setHgrow(c1, Priority.ALWAYS);
        GridPane.setHgrow(c2, Priority.ALWAYS);
        GridPane.setHgrow(c3, Priority.ALWAYS);
        GridPane.setHgrow(c4, Priority.ALWAYS);
        GridPane.setHgrow(c5, Priority.ALWAYS);

        grid.add(c1, 0, 0);
        grid.add(c2, 1, 0);
        grid.add(c3, 2, 0);
        grid.add(c4, 0, 1);
        grid.add(c5, 1, 1);

        contentBox.getChildren().addAll(messageLabel, grid);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    private VBox createCard(String title, String desc, Button btn) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        d.setWrapText(true);
        VBox.setVgrow(d, Priority.ALWAYS);

        btn.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().addAll(t, d, btn);
        return card;
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
