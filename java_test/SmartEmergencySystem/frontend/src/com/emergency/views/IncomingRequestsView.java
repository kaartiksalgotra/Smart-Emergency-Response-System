package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Admin view for reviewing, accepting, or rejecting incoming citizen emergency requests.
 */
public class IncomingRequestsView {

    private VBox root;
    private TableView<Emergency> requestsTable;
    private Button acceptButton, rejectButton, refreshButton;
    private Label messageLabel, detailIdLabel, detailTypeLabel, detailPriorityLabel, detailLocLabel, detailPeopleLabel, detailDescLabel;

    public IncomingRequestsView() {
        root = new VBox(14);
        root.getStyleClass().add("content-pane");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3, new Label("Incoming Emergency Requests"),
                new Label("Review newly reported emergencies. Accept to allow resource allocation or reject."));
        ((Label) titleBox.getChildren().get(0)).getStyleClass().add("page-title");
        ((Label) titleBox.getChildren().get(1)).getStyleClass().add("page-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        refreshButton = new Button("↻ Refresh Requests");
        refreshButton.getStyleClass().add("btn-secondary");
        header.getChildren().addAll(titleBox, spacer, refreshButton);

        // Table
        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(250);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Emergency, String> pplCol = new TableColumn<>("Affected");
        pplCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));

        TableColumn<Emergency, String> stCol = new TableColumn<>("Status");
        stCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); setText(null); }
                else { setGraphic(StatusIndicator.createStatusBadge(((Emergency) getTableRow().getItem()).getStatus())); setText(null); setAlignment(Pos.CENTER); }
            }
        });

        requestsTable.getColumns().addAll(idCol, userCol, typeCol, prioCol, locCol, pplCol, stCol);
        requestsTable.setPlaceholder(new Label("No pending emergency requests awaiting review."));

        // Details and Actions Card
        VBox actionCard = new VBox(10);
        actionCard.getStyleClass().add("card");
        Label cardTitle = new Label("Selected Request Details & Decision");
        cardTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(6);

        detailIdLabel = new Label("None selected");
        detailTypeLabel = new Label("-");
        detailPriorityLabel = new Label("-");
        detailLocLabel = new Label("-");
        detailPeopleLabel = new Label("-");
        detailDescLabel = new Label("-");
        detailDescLabel.setWrapText(true);

        grid.add(new Label("Request ID:"), 0, 0); grid.add(detailIdLabel, 1, 0);
        grid.add(new Label("Type:"), 2, 0); grid.add(detailTypeLabel, 3, 0);
        grid.add(new Label("Priority:"), 0, 1); grid.add(detailPriorityLabel, 1, 1);
        grid.add(new Label("Location:"), 2, 1); grid.add(detailLocLabel, 3, 1);
        grid.add(new Label("Affected:"), 0, 2); grid.add(detailPeopleLabel, 1, 2);
        grid.add(new Label("Description:"), 2, 2); grid.add(detailDescLabel, 3, 2);

        acceptButton = new Button("✔ ACCEPT REQUEST");
        acceptButton.getStyleClass().add("btn-primary");
        acceptButton.setPrefWidth(170); acceptButton.setDisable(true);

        rejectButton = new Button("✖ REJECT REQUEST");
        rejectButton.getStyleClass().add("btn-danger");
        rejectButton.setPrefWidth(170); rejectButton.setDisable(true);

        messageLabel = new Label("Select an emergency from the table above to review.");
        HBox btnRow = new HBox(12, acceptButton, rejectButton, messageLabel);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        actionCard.getChildren().addAll(cardTitle, grid, new Separator(), btnRow);

        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean has = sel != null;
            acceptButton.setDisable(!has); rejectButton.setDisable(!has);
            if (has) {
                detailIdLabel.setText("#" + sel.getEmergencyId());
                detailTypeLabel.setText(sel.getEmergencyType().toString());
                detailPriorityLabel.setText(sel.getPriority().toString());
                detailLocLabel.setText(sel.getLocation());
                detailPeopleLabel.setText(sel.getAffectedPeople() + " ppl");
                detailDescLabel.setText(sel.getDescription());
                messageLabel.setText("Ready to review Request #" + sel.getEmergencyId());
            } else {
                detailIdLabel.setText("None selected");
                detailDescLabel.setText("-");
            }
        });

        root.getChildren().addAll(header, requestsTable, actionCard);
    }

    public void setRequests(ArrayList<Emergency> list) { requestsTable.setItems(FXCollections.observableArrayList(list)); }
    public Emergency getSelectedEmergency() { return requestsTable.getSelectionModel().getSelectedItem(); }
    public Button getAcceptButton() { return acceptButton; }
    public Button getRejectButton() { return rejectButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Label getMessageLabel() { return messageLabel; }
    public VBox getView() { return root; }
}
