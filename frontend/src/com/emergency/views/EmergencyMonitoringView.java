package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.enums.EmergencyStatus;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;

/**
 * Admin view for monitoring all emergencies and updating incident operational statuses.
 * Modern blue theme with 1200px centered container and full API compatibility.
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
        root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24 32;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);

        // Header
        Label title = new Label("Emergency Incident Monitoring"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("Real-time monitoring of all reported emergencies and status lifecycle management."); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        refreshButton = new Button("🔄 Refresh Records"); refreshButton.getStyleClass().add("btn-primary");
        HBox header = new HBox(12, new VBox(2, title, sub), sp, refreshButton); header.setAlignment(Pos.CENTER_LEFT);

        // Table
        table = new TableView<>(); table.setPrefHeight(340); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Emergency, String> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId())); idCol.setMaxWidth(60);
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
        TableColumn<Emergency, String> descCol = new TableColumn<>("DESCRIPTION"); descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        TableColumn<Emergency, String> pplCol = new TableColumn<>("AFFECTED"); pplCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));
        TableColumn<Emergency, String> stCol = new TableColumn<>("STATUS"); stCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createStatusBadge(e.getStatus()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });
        table.getColumns().addAll(idCol, userCol, typeCol, prioCol, locCol, descCol, pplCol, stCol);
        table.setPlaceholder(new Label("No emergencies found."));

        VBox tableCard = new VBox(10, new Label("Active Incident Ledger") {{ setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); }}, table);
        tableCard.setPadding(new Insets(18)); tableCard.getStyleClass().add("card");

        // Status Update Card
        VBox updateCard = new VBox(12); updateCard.setPadding(new Insets(18)); updateCard.getStyleClass().add("card");
        Label cardTitle = new Label("Update Incident Status"); cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        selectedInfoLabel = new Label("No emergency selected from the table."); selectedInfoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll(EmergencyStatus.ACCEPTED, EmergencyStatus.ASSIGNED, EmergencyStatus.IN_PROGRESS, EmergencyStatus.RESOLVED, EmergencyStatus.CANCELLED);
        statusBox.setPromptText("Select New Status"); statusBox.setPrefWidth(220); statusBox.setPrefHeight(38);

        updateStatusButton = new Button("Apply Status Change"); updateStatusButton.getStyleClass().add("btn-primary"); updateStatusButton.setDisable(true);
        messageLabel = new Label("Select an emergency row to update its progress."); messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        HBox formRow = new HBox(12, new Label("New Status:") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;"); }}, statusBox, updateStatusButton, messageLabel);
        formRow.setAlignment(Pos.CENTER_LEFT);
        updateCard.getChildren().addAll(cardTitle, selectedInfoLabel, formRow);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            boolean has = sel != null;
            updateStatusButton.setDisable(!has);
            if (has) selectedInfoLabel.setText("Selected: Incident #" + sel.getEmergencyId() + " (" + sel.getEmergencyType() + " at " + sel.getLocation() + ") - Current Status: " + sel.getStatus());
            else selectedInfoLabel.setText("No emergency selected from the table.");
        });

        contentBox.getChildren().addAll(header, tableCard, updateCard);
        root.getChildren().add(contentBox);
    }

    public void setEmergencies(ArrayList<Emergency> list) { table.setItems(FXCollections.observableArrayList(list)); }
    public Emergency getSelectedEmergency() { return table.getSelectionModel().getSelectedItem(); }
    public ComboBox<EmergencyStatus> getStatusBox() { return statusBox; }
    public Button getUpdateStatusButton() { return updateStatusButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Label getMessageLabel() { return messageLabel; }
    public TableView<Emergency> getTable() { return table; }
    public VBox getView() { return root; }
}
