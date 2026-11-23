package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Resource Fleet Management View.
 * Add, update, view, and remove emergency vehicles and response teams.
 */
public class ResourceManagementView {

    private BorderPane root;
    private TableView<Resource> resourceTable;

    private TextField nameField;
    private TextField contactField;
    private TextField locationField;
    private TextField detailsField;
    private ComboBox<String> typeBox;

    private Button addButton;
    private Button updateButton;
    private Button removeButton;
    private Button refreshButton;
    private Button backButton;

    private Label messageLabel;

    public ResourceManagementView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Resource & Fleet Management");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Fleet");
        refreshButton.getStyleClass().add("btn-primary");
        refreshButton.setPrefHeight(36);

        topBar.getChildren().addAll(backButton, heading, spacer, refreshButton);
        root.setTop(topBar);

        // Center Content (Split Table + Form)
        VBox contentBox = new VBox(16);
        contentBox.getStyleClass().add("content-pane");

        HBox splitLayout = new HBox(16);

        // Table Card
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        resourceTable = new TableView<>();
        resourceTable.setPrefHeight(450);
        resourceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Resource, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getResourceId()));
        idCol.setMaxWidth(60);

        TableColumn<Resource, String> nameCol = new TableColumn<>("Unit Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResourceName()));

        TableColumn<Resource, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResourceType().toString()));

        TableColumn<Resource, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactNumber()));

        TableColumn<Resource, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Resource, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResourceStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Resource, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Resource res = (Resource) getTableRow().getItem();
                    setGraphic(StatusIndicator.createResourceStatusBadge(res.getResourceStatus()));
                    setText(null); setAlignment(Pos.CENTER);
                }
            }
        });

        resourceTable.getColumns().addAll(idCol, nameCol, typeCol, contactCol, locCol, statusCol);
        tableCard.getChildren().addAll(new Label("Active Resource Units"), resourceTable);

        // Form Card
        VBox formCard = new VBox(10);
        formCard.getStyleClass().add("card");
        formCard.setPrefWidth(350);

        nameField = new TextField();
        nameField.setPromptText("Resource name");

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("AMBULANCE", "FIRE_TRUCK", "RESCUE_TEAM");
        typeBox.setPromptText("Select Resource Type");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        contactField = new TextField();
        contactField.setPromptText("Contact number");

        locationField = new TextField();
        locationField.setPromptText("Station location");

        detailsField = new TextField();
        detailsField.setPromptText("Capacity / Crew members (e.g. 4)");

        addButton = new Button("+ Add Unit");
        addButton.getStyleClass().add("btn-success");
        HBox.setHgrow(addButton, Priority.ALWAYS);
        addButton.setMaxWidth(Double.MAX_VALUE);

        updateButton = new Button("💾 Update");
        updateButton.getStyleClass().add("btn-primary");
        HBox.setHgrow(updateButton, Priority.ALWAYS);
        updateButton.setMaxWidth(Double.MAX_VALUE);

        removeButton = new Button("🗑 Remove");
        removeButton.getStyleClass().add("btn-outline-danger");
        HBox.setHgrow(removeButton, Priority.ALWAYS);
        removeButton.setMaxWidth(Double.MAX_VALUE);

        HBox btnRow = new HBox(6, addButton, updateButton, removeButton);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        formCard.getChildren().addAll(
                new Label("Unit Specifications"),
                new Label("Fill details to add or select from table to modify:"),
                new VBox(2, new Label("Name:"), nameField),
                new VBox(2, new Label("Type:"), typeBox),
                new VBox(2, new Label("Contact:"), contactField),
                new VBox(2, new Label("Location:"), locationField),
                new VBox(2, new Label("Capacity / Crew Count:"), detailsField),
                btnRow,
                messageLabel
        );

        splitLayout.getChildren().addAll(tableCard, formCard);
        contentBox.getChildren().add(splitLayout);

        root.setCenter(contentBox);
    }

    public BorderPane getView() { return root; }
    public TableView<Resource> getResourceTable() { return resourceTable; }
    public TextField getNameField() { return nameField; }
    public TextField getContactField() { return contactField; }
    public TextField getLocationField() { return locationField; }
    public TextField getDetailsField() { return detailsField; }
    public ComboBox<String> getTypeBox() { return typeBox; }
    public Button getAddButton() { return addButton; }
    public Button getUpdateButton() { return updateButton; }
    public Button getRemoveButton() { return removeButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getBackButton() { return backButton; }
    public Label getMessageLabel() { return messageLabel; }
}
