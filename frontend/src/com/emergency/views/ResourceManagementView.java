package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ERAS Resource & Fleet Management View.
 * Centered 1200px container with uniform button styling and exact Resource model getters.
 */
public class ResourceManagementView {
    private BorderPane root;
    private TableView<Resource> resourceTable;
    private TextField nameField, contactField, locationField, detailsField;
    private ComboBox<String> typeBox;
    private Button addButton, updateButton, removeButton, refreshButton, backButton;
    private Label messageLabel;

    public ResourceManagementView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        backButton = new Button("← Back to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Resource & Fleet Management");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminPill = new Label("Admin View");
        adminPill.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 12px; -fx-font-size: 11px;");

        refreshButton = new Button("🔄 Refresh Fleet");
        refreshButton.getStyleClass().add("btn-primary");

        HBox headerContent = new HBox(16, backButton, heading, spacer, adminPill, refreshButton);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Center Content (centered, max-width 1200px)
        HBox split = new HBox(18, buildTableCard(), buildFormCard());
        split.setMaxWidth(1200);
        split.setPadding(new Insets(24, 32, 28, 32));

        StackPane centerWrapper = new StackPane(split);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    private VBox buildTableCard() {
        resourceTable = new TableView<>();
        resourceTable.setPrefHeight(480);
        resourceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Resource, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getResourceId()));
        idCol.setMaxWidth(60);

        TableColumn<Resource, String> nameCol = new TableColumn<>("UNIT NAME");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResourceName()));

        TableColumn<Resource, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResourceType().toString()));

        TableColumn<Resource, String> contactCol = new TableColumn<>("CONTACT");
        contactCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactNumber()));

        TableColumn<Resource, String> locCol = new TableColumn<>("BASE");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Resource, String> stCol = new TableColumn<>("STATUS");
        stCol.setCellFactory(c -> new TableCell<Resource, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Resource r = (empty || getTableRow() == null) ? null : (Resource) getTableRow().getItem();
                setGraphic(r != null ? StatusIndicator.createResourceStatusBadge(r.getResourceStatus()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        resourceTable.getColumns().addAll(idCol, nameCol, typeCol, contactCol, locCol, stCol);

        VBox card = new VBox(10, new Label("Fleet Inventory") {{ setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); }}, resourceTable);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox buildFormCard() {
        nameField = new TextField(); nameField.setPromptText("Resource name"); nameField.setPrefHeight(38);
        contactField = new TextField(); contactField.setPromptText("Contact number"); contactField.setPrefHeight(38);
        locationField = new TextField(); locationField.setPromptText("Station location"); locationField.setPrefHeight(38);
        detailsField = new TextField(); detailsField.setPromptText("Capacity / Crew members (e.g. 4)"); detailsField.setPrefHeight(38);

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("AMBULANCE", "FIRE_TRUCK", "RESCUE_TEAM");
        typeBox.setPromptText("Select Resource Type");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(38);

        addButton = new Button("+ Add Unit"); addButton.getStyleClass().add("btn-primary"); addButton.setPrefHeight(38);
        updateButton = new Button("✎ Update"); updateButton.getStyleClass().add("btn-secondary"); updateButton.setPrefHeight(38);
        removeButton = new Button("✕ Remove"); removeButton.getStyleClass().add("btn-danger"); removeButton.setPrefHeight(38);
        HBox btnRow = new HBox(8, addButton, updateButton, removeButton);

        messageLabel = new Label(""); messageLabel.setWrapText(true); messageLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        VBox form = new VBox(10,
                new Label("Unit Editor") {{ setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;"); }},
                new VBox(4, makeLbl("Unit Name"), nameField),
                new VBox(4, makeLbl("Unit Type"), typeBox),
                new VBox(4, makeLbl("Contact Number"), contactField),
                new VBox(4, makeLbl("Base Location"), locationField),
                new VBox(4, makeLbl("Capacity / Crew Count"), detailsField),
                btnRow, messageLabel
        );
        form.setPrefWidth(320);
        form.getStyleClass().add("card");
        form.setPadding(new Insets(18));
        return form;
    }

    private Label makeLbl(String text) {
        Label l = new Label(text); l.getStyleClass().add("field-label"); return l;
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
