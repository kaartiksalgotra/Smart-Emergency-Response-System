package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.enums.EmergencyStatus;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Admin view for monitoring all emergencies and updating incident operational statuses.
 */
public class EmergencyMonitoringView {

    private VBox root;
    private TableView<Emergency> table;
    private ComboBox<EmergencyStatus> statusBox;
    private Button updateStatusButton;
    private Button refreshButton;
    private Label messageLabel;
    private Label selectedInfoLabel;

    public EmergencyMonitoringView() {
        root = new VBox(15);
        root.getStyleClass().add("content-pane");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        Label title = new Label("Emergency Incident Monitoring");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Real-time monitoring of all reported emergencies and status lifecycle management.");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        refreshButton = new Button("↻ Refresh Records");
        refreshButton.getStyleClass().add("btn-secondary");
        header.getChildren().addAll(titleBox, spacer, refreshButton);

        // Table
        table = new TableView<>();
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(60);

        TableColumn<Emergency, String> userCol = new TableColumn<>("Citizen ID");
        userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));

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

        TableColumn<Emergency, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Emergency, String> peopleCol = new TableColumn<>("Affected");
        peopleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));

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

        table.getColumns().addAll(idCol, userCol, typeCol, priorityCol, locCol, descCol, peopleCol, statusCol);
        table.setPlaceholder(new Label("No emergencies found."));

        // Status Update Card
        VBox updateCard = new VBox(10);
        updateCard.getStyleClass().add("card");

        Label cardTitle = new Label("Update Incident Status");
        cardTitle.getStyleClass().add("section-title");

        selectedInfoLabel = new Label("No emergency selected from the table.");
        selectedInfoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");

        HBox formRow = new HBox(12);
        formRow.setAlignment(Pos.CENTER_LEFT);

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll(
                EmergencyStatus.ACCEPTED,
                EmergencyStatus.ASSIGNED,
                EmergencyStatus.IN_PROGRESS,
                EmergencyStatus.RESOLVED,
                EmergencyStatus.CANCELLED
        );
        statusBox.setPromptText("Select New Status");
        statusBox.setPrefWidth(200);

        updateStatusButton = new Button("Apply Status Change");
        updateStatusButton.getStyleClass().add("btn-primary");
        updateStatusButton.setDisable(true);

        messageLabel = new Label("Select an emergency row to update its progress.");
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        formRow.getChildren().addAll(new Label("New Status:"), statusBox, updateStatusButton, messageLabel);
        updateCard.getChildren().addAll(cardTitle, selectedInfoLabel, formRow);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            boolean has = sel != null;
            updateStatusButton.setDisable(!has);
            if (has) {
                selectedInfoLabel.setText("Selected: Incident #" + sel.getEmergencyId() + " (" + sel.getEmergencyType() + " at " + sel.getLocation() + ") - Current Status: " + sel.getStatus());
            } else {
                selectedInfoLabel.setText("No emergency selected from the table.");
            }
        });

        root.getChildren().addAll(header, table, updateCard);
    }

    public void setEmergencies(ArrayList<Emergency> list) {
        table.setItems(FXCollections.observableArrayList(list));
    }

    public Emergency getSelectedEmergency() { return table.getSelectionModel().getSelectedItem(); }
    public ComboBox<EmergencyStatus> getStatusBox() { return statusBox; }
    public Button getUpdateStatusButton() { return updateStatusButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Label getMessageLabel() { return messageLabel; }
    public VBox getView() { return root; }
}
