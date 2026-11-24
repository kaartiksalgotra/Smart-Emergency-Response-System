package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Emergency Tracking View Component.
 * Real-time tracking view centered within a 1200px container with uniform button styling.
 */
public class TrackEmergencyView {

    private BorderPane root;
    private TableView<Emergency> emergencyTable;
    private Button refreshButton;
    private Button backButton;
    private Label heading;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public TrackEmergencyView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        heading = new Label("Track Your Emergencies");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Records");
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
        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);
        contentBox.setPadding(new Insets(24, 32, 28, 32));

        Label workflowHint = new Label("Workflow: REQUESTED → ACCEPTED → ASSIGNED → IN PROGRESS → RESOLVED");
        workflowHint.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        emergencyTable = new TableView<>();
        emergencyTable.setPrefHeight(450);
        emergencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("Incident ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(90);

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

        TableColumn<Emergency, String> dateCol = new TableColumn<>("Reported At");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));

        TableColumn<Emergency, String> statusCol = new TableColumn<>("Status");
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

        emergencyTable.getColumns().addAll(idCol, typeCol, priorityCol, locCol, dateCol, statusCol);
        emergencyTable.setPlaceholder(new Label("No active emergencies found."));

        tableCard.getChildren().addAll(workflowHint, emergencyTable);
        contentBox.getChildren().add(tableCard);

        StackPane centerWrapper = new StackPane(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    public BorderPane getView() { return root; }
    public TableView<Emergency> getEmergencyTable() { return emergencyTable; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getBackButton() { return backButton; }
    public Label getHeading() { return heading; }
}
