package com.emergency.views;

import com.emergency.model.Allocation;
import com.emergency.model.Emergency;
import com.emergency.model.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import java.util.ArrayList;

/**
 * Admin view for Auto and Manual Resource Allocation to ACCEPTED emergencies.
 * Modern blue theme with 1200px centered container and full API compatibility.
 */
public class ResourceAllocationView {
    private VBox root;
    private ComboBox<Emergency> emergencyBox;
    private ComboBox<Resource> resourceBox;
    private Button autoAssignButton, manualAssignButton, releaseButton;
    private Label messageLabel;
    private TableView<Allocation> allocationTable;

    public ResourceAllocationView() {
        root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24 32;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);

        // Header
        VBox header = new VBox(3);
        Label title = new Label("Resource Allocation Command"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label subtitle = new Label("Dispatch response units (Ambulance, Fire Truck, Rescue Team) to ACCEPTED emergencies."); subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        header.getChildren().addAll(title, subtitle);

        // Allocation Controls Card
        VBox allocCard = new VBox(14); allocCard.getStyleClass().add("card"); allocCard.setPadding(new Insets(20));
        Label cardTitle = new Label("Dispatch Options"); cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(12);
        emergencyBox = new ComboBox<>(); emergencyBox.setPromptText("Select Accepted Emergency"); emergencyBox.setPrefWidth(320); emergencyBox.setPrefHeight(38);
        emergencyBox.setConverter(new StringConverter<Emergency>() {
            @Override public String toString(Emergency e) { return e == null ? "" : "#" + e.getEmergencyId() + " - " + e.getEmergencyType() + " (" + e.getPriority() + " at " + e.getLocation() + ")"; }
            @Override public Emergency fromString(String s) { return null; }
        });

        resourceBox = new ComboBox<>(); resourceBox.setPromptText("Select Available Resource"); resourceBox.setPrefWidth(320); resourceBox.setPrefHeight(38);
        resourceBox.setConverter(new StringConverter<Resource>() {
            @Override public String toString(Resource r) { return r == null ? "" : "#" + r.getResourceId() + " - " + r.getResourceName() + " (" + r.getResourceType() + ")"; }
            @Override public Resource fromString(String s) { return null; }
        });

        autoAssignButton = new Button("⚡ Auto-Allocate Best Unit"); autoAssignButton.getStyleClass().add("btn-primary");
        manualAssignButton = new Button("Assign Selected Resource"); manualAssignButton.getStyleClass().add("btn-secondary");

        grid.add(new Label("Target Emergency (ACCEPTED):") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;"); }}, 0, 0);
        grid.add(emergencyBox, 1, 0); grid.add(autoAssignButton, 2, 0);
        grid.add(new Label("Manual Resource Selection:") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;"); }}, 0, 1);
        grid.add(resourceBox, 1, 1); grid.add(manualAssignButton, 2, 1);

        messageLabel = new Label("Select an accepted emergency to allocate resources."); messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        allocCard.getChildren().addAll(cardTitle, grid, messageLabel);

        // Active Allocations Table Card
        VBox tableCard = new VBox(10); tableCard.getStyleClass().add("card"); tableCard.setPadding(new Insets(18));
        HBox tableHeader = new HBox(12); tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Active Response Unit Deployments"); tableTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        releaseButton = new Button("Return / Release Selected Resource"); releaseButton.getStyleClass().add("btn-danger");
        tableHeader.getChildren().addAll(tableTitle, spacer, releaseButton);

        allocationTable = new TableView<>(); allocationTable.setPrefHeight(250); allocationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Allocation, String> allocIdCol = new TableColumn<>("Alloc ID"); allocIdCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getAllocationId())); allocIdCol.setMaxWidth(80);
        TableColumn<Allocation, String> emIdCol = new TableColumn<>("Emergency ID"); emIdCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        TableColumn<Allocation, String> resIdCol = new TableColumn<>("Resource ID"); resIdCol.setCellValueFactory(d -> new SimpleStringProperty("Unit #" + d.getValue().getResourceId()));
        TableColumn<Allocation, String> timeCol = new TableColumn<>("Allocated At");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAssignedAt() != null ? d.getValue().getAssignedAt().toString() : ""));
        TableColumn<Allocation, String> statusCol = new TableColumn<>("Active Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "DEPLOYED" : "RELEASED"));
        allocationTable.getColumns().addAll(allocIdCol, emIdCol, resIdCol, timeCol, statusCol);
        allocationTable.setPlaceholder(new Label("No units currently deployed."));

        tableCard.getChildren().addAll(tableHeader, allocationTable);
        contentBox.getChildren().addAll(header, allocCard, tableCard);
        root.getChildren().add(contentBox);
    }

    public void setAcceptedEmergencies(ArrayList<Emergency> list) { emergencyBox.setItems(FXCollections.observableArrayList(list)); }
    public void setAvailableResources(ArrayList<Resource> list) { resourceBox.setItems(FXCollections.observableArrayList(list)); }
    public void setAllocations(ArrayList<Allocation> list) { allocationTable.setItems(FXCollections.observableArrayList(list)); }
    public ComboBox<Emergency> getEmergencyBox() { return emergencyBox; }
    public ComboBox<Resource> getResourceBox() { return resourceBox; }
    public Button getAutoAssignButton() { return autoAssignButton; }
    public Button getManualAssignButton() { return manualAssignButton; }
    public Button getReleaseButton() { return releaseButton; }
    public Label getMessageLabel() { return messageLabel; }
    public TableView<Allocation> getAllocationTable() { return allocationTable; }
    public VBox getView() { return root; }
}
