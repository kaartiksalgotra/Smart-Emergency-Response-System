package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Citizen 'My Requests' View.
 * Displays all emergency requests submitted by citizen in a centered 1200px container.
 */
public class MyRequestsView {

    private BorderPane root;
    private TableView<Emergency> requestsTable;
    private Button refreshButton;
    private Button backButton;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public MyRequestsView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("My Submitted Requests");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Requests");
        refreshButton.getStyleClass().add("btn-primary");

        HBox headerContent = new HBox(16, backButton, heading, spacer, refreshButton);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Center Content Container (centered, max-width 1200px)
        VBox contentBox = new VBox(14);
        contentBox.setMaxWidth(1200);
        contentBox.setPadding(new Insets(24, 32, 28, 32));

        Label workflowHint = new Label("Status Guide: REQUESTED (Awaiting Review) → ACCEPTED / REJECTED → ASSIGNED → IN PROGRESS → RESOLVED");
        workflowHint.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(450);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(70);

        TableColumn<Emergency, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));

        TableColumn<Emergency, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        priorityCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Emergency em = (Emergency) getTableRow().getItem();
                    setGraphic(StatusIndicator.createPriorityBadge(em.getPriority()));
                    setText(null); setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> dateCol = new TableColumn<>("Submitted At");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));

        TableColumn<Emergency, String> statusCol = new TableColumn<>("Current Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Emergency em = (Emergency) getTableRow().getItem();
                    setGraphic(StatusIndicator.createStatusBadge(em.getStatus()));
                    setText(null); setAlignment(Pos.CENTER);
                }
            }
        });

        requestsTable.getColumns().addAll(idCol, typeCol, priorityCol, locCol, dateCol, statusCol);
        requestsTable.setPlaceholder(new Label("You have not submitted any emergency requests yet."));

        tableCard.getChildren().addAll(workflowHint, requestsTable);
        contentBox.getChildren().add(tableCard);

        StackPane centerWrapper = new StackPane(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    public void setRequests(ArrayList<Emergency> list) {
        requestsTable.setItems(FXCollections.observableArrayList(list));
    }

    public BorderPane getView() { return root; }
    public TableView<Emergency> getRequestsTable() { return requestsTable; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getBackButton() { return backButton; }
}
