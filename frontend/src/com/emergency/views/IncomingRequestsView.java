package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;

/**
 * Admin view for reviewing, accepting, or rejecting incoming citizen emergency requests.
 * Modern blue theme with 1200px centered container and full API compatibility.
 */
public class IncomingRequestsView {
    private VBox root;
    private TableView<Emergency> requestsTable;
    private Button acceptButton, rejectButton, refreshButton;
    private Label messageLabel, detailIdLabel, detailTypeLabel, detailPriorityLabel, detailLocLabel, detailPeopleLabel, detailDescLabel;

    public IncomingRequestsView() {
        root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24 32;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);

        // Header
        Label title = new Label("Incoming Emergency Requests"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("Review newly reported emergencies. Accept to allow resource allocation or reject."); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        refreshButton = new Button("🔄 Refresh Requests"); refreshButton.getStyleClass().add("btn-primary");
        HBox header = new HBox(12, new VBox(2, title, sub), sp, refreshButton); header.setAlignment(Pos.CENTER_LEFT);

        // Table Card
        VBox tableCard = new VBox(10, new Label("Pending Citizen Requests") {{ setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); }});
        tableCard.setPadding(new Insets(18)); tableCard.getStyleClass().add("card");

        requestsTable = new TableView<>(); requestsTable.setPrefHeight(280); requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Emergency, String> idCol = new TableColumn<>("INCIDENT ID"); idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId())); idCol.setMaxWidth(80);
        TableColumn<Emergency, String> userCol = new TableColumn<>("CITIZEN"); userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));
        TableColumn<Emergency, String> typeCol = new TableColumn<>("TYPE"); typeCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createTypeBadge(e.getEmergencyType()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });
        TableColumn<Emergency, String> prioCol = new TableColumn<>("PRIORITY"); prioCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createPriorityBadge(e.getPriority()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });
        TableColumn<Emergency, String> locCol = new TableColumn<>("LOCATION"); locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));
        TableColumn<Emergency, String> pplCol = new TableColumn<>("AFFECTED"); pplCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));
        TableColumn<Emergency, String> stCol = new TableColumn<>("STATUS"); stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createStatusBadge(e.getStatus()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });

        requestsTable.getColumns().addAll(idCol, userCol, typeCol, prioCol, locCol, pplCol, stCol);
        requestsTable.setPlaceholder(new Label("No pending emergency requests awaiting review."));
        tableCard.getChildren().add(requestsTable);

        // Details & Decision Card
        VBox decisionCard = new VBox(12); decisionCard.setPadding(new Insets(18)); decisionCard.getStyleClass().add("card");
        Label dTitle = new Label("Selected Request Details & Decision"); dTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        detailIdLabel = makeVal("None selected"); detailTypeLabel = makeVal("-"); detailPriorityLabel = makeVal("-");
        detailLocLabel = makeVal("-"); detailPeopleLabel = makeVal("-"); detailDescLabel = makeVal("-");

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(8);
        grid.add(makeKey("Request ID:"), 0, 0); grid.add(detailIdLabel, 1, 0);
        grid.add(makeKey("Type:"), 2, 0); grid.add(detailTypeLabel, 3, 0);
        grid.add(makeKey("Priority:"), 0, 1); grid.add(detailPriorityLabel, 1, 1);
        grid.add(makeKey("Location:"), 2, 1); grid.add(detailLocLabel, 3, 1);
        grid.add(makeKey("Affected:"), 0, 2); grid.add(detailPeopleLabel, 1, 2);
        grid.add(makeKey("Description:"), 2, 2); grid.add(detailDescLabel, 3, 2);

        acceptButton = new Button("✔ ACCEPT REQUEST"); acceptButton.getStyleClass().add("btn-primary"); acceptButton.setPrefWidth(180); acceptButton.setDisable(true);
        rejectButton = new Button("✖ REJECT REQUEST"); rejectButton.getStyleClass().add("btn-danger"); rejectButton.setPrefWidth(180); rejectButton.setDisable(true);
        messageLabel = new Label("Select an emergency from the table above to review."); messageLabel.setWrapText(true); messageLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        decisionCard.getChildren().addAll(dTitle, grid, new HBox(12, acceptButton, rejectButton, messageLabel));

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
                detailIdLabel.setText("None selected"); detailDescLabel.setText("-");
            }
        });

        contentBox.getChildren().addAll(header, tableCard, decisionCard);
        root.getChildren().add(contentBox);
    }

    private Label makeKey(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;"); return l;
    }
    private Label makeVal(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); return l;
    }

    public void setRequests(ArrayList<Emergency> list) { requestsTable.setItems(FXCollections.observableArrayList(list)); }
    public Emergency getSelectedEmergency() { return requestsTable.getSelectionModel().getSelectedItem(); }
    public TableView<Emergency> getRequestsTable() { return requestsTable; }
    public Button getAcceptButton() { return acceptButton; }
    public Button getRejectButton() { return rejectButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Label getMessageLabel() { return messageLabel; }
    public Label getDetailIdLabel() { return detailIdLabel; }
    public Label getDetailTypeLabel() { return detailTypeLabel; }
    public Label getDetailPriorityLabel() { return detailPriorityLabel; }
    public Label getDetailLocLabel() { return detailLocLabel; }
    public Label getDetailPeopleLabel() { return detailPeopleLabel; }
    public Label getDetailDescLabel() { return detailDescLabel; }
    public VBox getView() { return root; }
}
