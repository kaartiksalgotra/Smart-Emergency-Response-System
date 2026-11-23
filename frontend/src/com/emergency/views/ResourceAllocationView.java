package com.emergency.views;

import com.emergency.enums.EmergencyType;
import com.emergency.enums.ResourceType;
import com.emergency.model.Allocation;
import com.emergency.model.Emergency;
import com.emergency.model.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.ArrayList;

/**
 * Admin view for Auto and Manual Resource Allocation to ACCEPTED emergencies.
 */
public class ResourceAllocationView {

    private VBox root;
    private ComboBox<Emergency> emergencyBox;
    private ComboBox<Resource> resourceBox;
    private Button autoAssignButton;
    private Button manualAssignButton;
    private Button releaseButton;
    private Label messageLabel;
    private TableView<Allocation> allocationTable;

    public ResourceAllocationView() {
        root = new VBox(15);
        root.getStyleClass().add("content-pane");

        // Header
        VBox header = new VBox(3);
        Label title = new Label("Resource Allocation Command");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Dispatch response units (Ambulance, Fire Truck, Rescue Team) to ACCEPTED emergencies.");
        subtitle.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Allocation Controls Card
        VBox allocCard = new VBox(12);
        allocCard.getStyleClass().add("card");

        Label cardTitle = new Label("Dispatch Options");
        cardTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        emergencyBox = new ComboBox<>();
        emergencyBox.setPromptText("Select Accepted Emergency");
        emergencyBox.setPrefWidth(300);
        emergencyBox.setConverter(new StringConverter<Emergency>() {
            @Override public String toString(Emergency e) {
                return e == null ? "" : "#" + e.getEmergencyId() + " - " + e.getEmergencyType() + " (" + e.getPriority() + " at " + e.getLocation() + ")";
            }
            @Override public Emergency fromString(String s) { return null; }
        });

        resourceBox = new ComboBox<>();
        resourceBox.setPromptText("Select Available Resource");
        resourceBox.setPrefWidth(300);
        resourceBox.setConverter(new StringConverter<Resource>() {
            @Override public String toString(Resource r) {
                return r == null ? "" : "#" + r.getResourceId() + " - " + r.getResourceName() + " (" + r.getResourceType() + ")";
            }
            @Override public Resource fromString(String s) { return null; }
        });

        autoAssignButton = new Button("⚡ Auto-Allocate Best Unit");
        autoAssignButton.getStyleClass().add("btn-primary");
        autoAssignButton.setPrefHeight(36);

        manualAssignButton = new Button("Assign Selected Resource");
        manualAssignButton.getStyleClass().add("btn-secondary");
        manualAssignButton.setPrefHeight(36);

        grid.add(new Label("Target Emergency (ACCEPTED):"), 0, 0);
        grid.add(emergencyBox, 1, 0);
        grid.add(autoAssignButton, 2, 0);

        grid.add(new Label("Manual Resource Selection:"), 0, 1);
        grid.add(resourceBox, 1, 1);
        grid.add(manualAssignButton, 2, 1);

        messageLabel = new Label("Select an accepted emergency to allocate resources.");
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        allocCard.getChildren().addAll(cardTitle, grid, messageLabel);

        // Active Allocations Table Card
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Active Response Unit Deployments");
        tableTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        releaseButton = new Button("Return / Release Selected Resource");
        releaseButton.getStyleClass().add("btn-danger");
        tableHeader.getChildren().addAll(tableTitle, spacer, releaseButton);

        allocationTable = new TableView<>();
        allocationTable.setPrefHeight(230);
        allocationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Allocation, String> allocIdCol = new TableColumn<>("Alloc ID");
        allocIdCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getAllocationId()));
        allocIdCol.setMaxWidth(80);

        TableColumn<Allocation, String> emIdCol = new TableColumn<>("Emergency ID");
        emIdCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));

        TableColumn<Allocation, String> resIdCol = new TableColumn<>("Resource ID");
        resIdCol.setCellValueFactory(d -> new SimpleStringProperty("Unit #" + d.getValue().getResourceId()));

        TableColumn<Allocation, String> timeCol = new TableColumn<>("Allocated At");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAssignedAt() != null ? d.getValue().getAssignedAt().toString() : ""
        ));

        TableColumn<Allocation, String> statusCol = new TableColumn<>("Active Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "DEPLOYED" : "RELEASED"));

        allocationTable.getColumns().addAll(allocIdCol, emIdCol, resIdCol, timeCol, statusCol);
        allocationTable.setPlaceholder(new Label("No units currently deployed."));

        tableCard.getChildren().addAll(tableHeader, allocationTable);
        root.getChildren().addAll(header, allocCard, tableCard);
    }

    public void setAcceptedEmergencies(ArrayList<Emergency> list) {
        emergencyBox.setItems(FXCollections.observableArrayList(list));
    }

    public void setAvailableResources(ArrayList<Resource> list) {
        resourceBox.setItems(FXCollections.observableArrayList(list));
    }

    public void setAllocations(ArrayList<Allocation> list) {
        allocationTable.setItems(FXCollections.observableArrayList(list));
    }

    public ComboBox<Emergency> getEmergencyBox() { return emergencyBox; }
    public ComboBox<Resource> getResourceBox() { return resourceBox; }
    public Button getAutoAssignButton() { return autoAssignButton; }
    public Button getManualAssignButton() { return manualAssignButton; }
    public Button getReleaseButton() { return releaseButton; }
    public Label getMessageLabel() { return messageLabel; }
    public TableView<Allocation> getAllocationTable() { return allocationTable; }
    public VBox getView() { return root; }
}
